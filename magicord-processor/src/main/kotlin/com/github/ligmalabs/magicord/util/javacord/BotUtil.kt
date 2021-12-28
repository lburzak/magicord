package com.github.ligmalabs.magicord.util.javacord

import com.github.ligmalabs.magicord.api.Channel
import com.github.ligmalabs.magicord.api.User
import org.javacord.api.event.message.MessageCreateEvent

fun MessageCreateEvent.readChannel(): Channel = Channel(
    id = channel.idAsString
)

fun MessageCreateEvent.readAuthor(): User = User(
    id = messageAuthor.idAsString,
    mentionTag = messageAuthor.asUser().get().mentionTag
)