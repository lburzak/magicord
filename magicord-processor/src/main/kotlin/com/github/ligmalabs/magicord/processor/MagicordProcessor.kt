package com.github.ligmalabs.magicord.processor

import com.github.ligmalabs.magicord.annotation.Bot
import com.github.ligmalabs.magicord.annotation.Command
import com.github.ligmalabs.magicord.api.Channel
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.event.message.MessageCreateEvent
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class MagicordProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.github.ligmalabs.magicord.annotation.Bot")
        val invalidSymbols = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(Visitor(), Unit) }
        return invalidSymbols
    }

    private fun log(message: Any?) {
        logger.logging("Magicord-Processor $message", null)
    }

    private fun KSClassDeclaration.readBotConfig(): BotConfig {
        val botAnnotation = annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Bot::class.qualifiedName
            }

        val token = botAnnotation.arguments.firstOrNull { it.name?.asString() == "token" }?.value.toString()

        return BotConfig(
            token = token
        )
    }

    fun createRunFun(botConfig: BotConfig, commandHandlers: Sequence<FunSpec>): FunSpec {
        val runBuilder = FunSpec.builder("run")
            .addStatement("println(%S)", "Running a bot")
            .addStatement("val api = %T()", DiscordApiBuilder::class)
            .addStatement("api.setToken(%S).login().join()", botConfig.token)

        commandHandlers.forEach { handler ->
            runBuilder.addStatement(
                "api.addMessageCreateListener(%M(%S, ::%N))", buildPrefixHandlerMember, handler.name, handler
            )
        }

        return runBuilder.build()
    }

    fun makeBotClass(botConfig: BotConfig, className: String, classDeclaration: KSClassDeclaration): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(className)

        classBuilder.addProperty(createBotInstanceProperty(classDeclaration))

        val commandHandlers = classDeclaration.getDeclaredFunctions()
            .filter { it.isCommandHandler() }
            .map { it.toCommandHandler() }

        classBuilder
            .addFunction(createRunFun(botConfig, commandHandlers))

        commandHandlers.forEach { handler ->
            classBuilder.addFunction(handler)
        }

        return classBuilder
            .build()
    }

    fun KSFunctionDeclaration.toCommandHandler(): FunSpec {
        val command = simpleName.asString()
        return FunSpec.builder(command)
            .addModifiers(KModifier.PRIVATE)
            .addParameter("event", MessageCreateEvent::class)
            .addStatement("val channel = %T(event.channel.idAsString)", Channel::class)
            .addStatement("return bot.`%L`(%L)", command, if (hasChannelDependency()) "channel" else "")
            .returns(String::class)
            .build()
    }

    fun KSClassDeclaration.guessClass(): ClassName =
        ClassName.bestGuess(qualifiedName?.asString() ?: "")

    fun createBotInstanceProperty(classDeclaration: KSClassDeclaration): PropertySpec {
        val botClass = classDeclaration.guessClass()
        return PropertySpec.builder("bot", botClass)
            .initializer("%T()", botClass)
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private val buildPrefixHandlerMember = MemberName(
        "com.github.ligmalabs.magicord.util.javacord",
        "buildPrefixCommandHandler"
    )

    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = "Magicord${classDeclaration.simpleName.asString()}"

            val botConfig = classDeclaration.readBotConfig()
            val botType = makeBotClass(botConfig, className, classDeclaration)

            FileSpec.builder(packageName, className)
                .addType(botType)
                .build()
                .writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
        }
    }

    private fun KSFunctionDeclaration.isCommandHandler(): Boolean =
        annotations.firstOrNull { annotation -> annotation.annotationType.resolve().declaration.qualifiedName?.asString() == Command::class.qualifiedName } != null

    private fun KSValueParameter.isChannelParameter(): Boolean =
        type.resolve().declaration.qualifiedName?.asString() == Channel::class.qualifiedName

    fun KSFunctionDeclaration.hasChannelDependency(): Boolean =
        parameters.any { it.isChannelParameter() }

    fun FileSpec.writeTo(codeGenerator: CodeGenerator, dependencies: Dependencies = Dependencies(false)) {
        val file = codeGenerator.createNewFile(dependencies, packageName, name)
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
    }
}

data class BotConfig(val token: String = "")