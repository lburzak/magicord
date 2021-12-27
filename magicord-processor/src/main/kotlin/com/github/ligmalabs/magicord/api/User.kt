package com.github.ligmalabs.magicord.api

class User(private val id: String) {
    fun makeResponse(content: String): String {
        return "$id $content"
    }
}