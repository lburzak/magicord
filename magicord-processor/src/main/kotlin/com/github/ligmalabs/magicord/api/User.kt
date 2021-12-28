package com.github.ligmalabs.magicord.api

class User(private val id: String, private val mentionTag: String) {
    fun makeResponse(content: String): String {
        return "$mentionTag $content"
    }
}