package ru.features.book
import kotlinx.serialization.Serializable

@Serializable
data class BookRemote(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val pages: Int,
    val coverUrl: String,
    val description: String
)