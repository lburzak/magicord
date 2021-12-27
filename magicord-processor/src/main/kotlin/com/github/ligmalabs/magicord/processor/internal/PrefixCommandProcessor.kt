package com.github.ligmalabs.magicord.processor.internal

import com.github.ligmalabs.magicord.api.Channel
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import org.javacord.api.event.message.MessageCreateEvent

internal class PrefixCommandProcessor {
    fun buildHandler(functionDeclaration: KSFunctionDeclaration): FunSpec {
        val commandName = functionDeclaration.simpleName.asString()

        val code = CodeBlock.builder()
            .add("return bot.`%L`(", commandName)

        if (functionDeclaration.hasChannelParameter())
            code.add("channel = event.%M()", readChannelMember)

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

    private fun KSFunctionDeclaration.hasChannelParameter(): Boolean =
        parameters.any { it.isChannelParameter() }

    private fun KSValueParameter.isChannelParameter(): Boolean =
        type.resolve().declaration.qualifiedName?.asString() == Channel::class.qualifiedName
}