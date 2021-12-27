package com.github.ligmalabs.magicord.processor.internal

import com.github.ligmalabs.magicord.api.Channel
import com.github.ligmalabs.magicord.api.User
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import org.javacord.api.event.message.MessageCreateEvent
import kotlin.reflect.KClass

internal class PrefixCommandProcessor {
    fun buildHandler(functionDeclaration: KSFunctionDeclaration): FunSpec {
        val commandName = functionDeclaration.simpleName.asString()

        val code = CodeBlock.builder()
            .add("return bot.`%L`(", commandName)

        if (functionDeclaration.hasParameterOfType(Channel::class))
            code.add("channel = event.%M(), ", readChannelMember)

        if (functionDeclaration.hasParameterOfType(User::class))
            code.add("author = event.%M(), ", readAuthorMember)

        code.add(")")

        return FunSpec.builder(commandName)
            .addModifiers(KModifier.PRIVATE)
            .addParameter("event", MessageCreateEvent::class)
            .addCode(code.build())
            .returns(String::class)
            .build()
    }

    private val readChannelMember = MemberName(
        "com.github.ligmalabs.magicord.util.javacord",
        "readChannel"
    )

    private val readAuthorMember = MemberName(
        "com.github.ligmalabs.magicord.util.javacord",
        "readAuthor"
    )

    private fun <T: Any> KSFunctionDeclaration.hasParameterOfType(type: KClass<T>) =
        parameters.any { it.type.resolve().declaration.qualifiedName?.asString() == type.qualifiedName }
}