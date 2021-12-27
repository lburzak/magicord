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

    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = "Magicord${classDeclaration.simpleName.asString()}"
            val fileKotlinPoet = FileSpec.builder(packageName, className)

            val botConfig = classDeclaration.readBotConfig()

            val functions = classDeclaration.getDeclaredFunctions().filter { it.isCommandHandler() }

            val classBuilder = TypeSpec.classBuilder(className)
            val botClass = ClassName.bestGuess(classDeclaration.qualifiedName?.asString() ?: "")

            val propertyBuilder = PropertySpec.builder("bot", botClass)
            propertyBuilder.initializer("%T()", botClass)
            propertyBuilder.addModifiers(KModifier.PRIVATE)
            classBuilder.addProperty(propertyBuilder.build())

            val runBuilder = FunSpec.builder("run")

            runBuilder
                .addStatement("println(%S)", "Running a bot")
                .addStatement("val api = %T()", DiscordApiBuilder::class)
                .addStatement("api.setToken(%S).login().join()", botConfig.token)

            functions.forEach {
                val command = it.simpleName.asString()
                val code = """
                api.addMessageCreateListener { event: %T ->
                         if (event.messageContent.equals(%S, ignoreCase = true)) {
                            val channel = %T(event.channel.idAsString)
                            val response = bot.`%L`(%L)
                            event.channel.sendMessage(response)
                        }
                    }
                """.trimIndent()
                runBuilder.addStatement(code, MessageCreateEvent::class, "!$command", Channel::class, command, if (it.hasChannelDependency()) "channel" else "")
            }

            classBuilder
                .addFunction(
                    runBuilder.build()
                )

            fileKotlinPoet.addType(classBuilder.build()).build()
            fileKotlinPoet.build().writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
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