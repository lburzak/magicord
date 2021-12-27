package com.github.ligmalabs.magicord.processor.internal

import com.github.ligmalabs.magicord.api.Channel
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import org.javacord.api.event.message.MessageCreateEvent

internal class CommandProcessor {
    fun buildHandler(functionDeclaration: KSFunctionDeclaration): FunSpec {
        val commandName = functionDeclaration.simpleName.asString()
        return FunSpec.builder(commandName)
            .addModifiers(KModifier.PRIVATE)
            .addParameter("event", MessageCreateEvent::class)
            .addStatement("val channel = %T(event.channel.idAsString)", Channel::class)
            .addStatement("return bot.`%L`(%L)", commandName, if (functionDeclaration.hasChannelParameter()) "channel" else "")
            .returns(String::class)
            .build()
    }

    private fun KSFunctionDeclaration.hasChannelParameter(): Boolean =
        parameters.any { it.isChannelParameter() }

    private fun KSValueParameter.isChannelParameter(): Boolean =
        type.resolve().declaration.qualifiedName?.asString() == Channel::class.qualifiedName
}