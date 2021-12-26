package com.github.ligmalabs.magicord.example

import com.github.ligmalabs.magicord.annotation.Bot
import com.github.ligmalabs.magicord.annotation.Command
import com.github.ligmalabs.magicord.api.Channel

@Bot(token = "your token")
class TestBot {
    @Command
    fun ping(channel: Channel): String {
        return "Pong in ${channel.id}"
    }

    @Command
    fun `show status`(): String = "Online :)"
}