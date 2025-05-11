package com.example.frontend

data class BookRemote(
    val id: Int,
    val title: String,
    val author: String,
    val genre: String,
    val pages: Int,
    val coverUrl: String,
    val description: String
)