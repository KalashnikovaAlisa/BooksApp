package ru.database.books
import kotlinx.serialization.Serializable

@Serializable
data class BookDTO(
    val idBook: String,
    val title: String,
    val author: String,
    val genre: String,
    val pages: Int,
    val coverUrl: String,
    val description: String
)
