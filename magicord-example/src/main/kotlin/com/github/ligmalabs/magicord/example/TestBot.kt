package com.github.ligmalabs.magicord.example

import com.github.ligmalabs.magicord.annotation.Bot
import com.github.ligmalabs.magicord.annotation.PrefixCommand
import com.github.ligmalabs.magicord.api.Channel

@Bot(token = "your token")
class TestBot {
    @PrefixCommand
    fun ping(channel: Channel): String {
        return "Pong in ${channel.id}"
    }

    @PrefixCommand
    fun `show status`(): String = "Online :)"
}