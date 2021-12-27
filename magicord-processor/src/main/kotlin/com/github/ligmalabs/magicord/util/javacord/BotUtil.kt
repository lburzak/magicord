package com.github.ligmalabs.magicord.util.javacord

import com.github.ligmalabs.magicord.api.Channel
import org.javacord.api.event.message.MessageCreateEvent

fun buildPrefixCommandHandler(command: String, handler: (MessageCreateEvent) -> String): (event: MessageCreateEvent) -> Unit {
    return { event ->
        if (event.messageContent.equals("!$command", ignoreCase = true)) {
            val response = handler(event)
            event.channel.sendMessage(response)
        }
    }
}

fun MessageCreateEvent.readChannel(): Channel = Channel(
    id = channel.idAsString
)