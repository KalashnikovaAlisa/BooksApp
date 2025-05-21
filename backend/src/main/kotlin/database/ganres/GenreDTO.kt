package ru.database.ganres

import kotlinx.serialization.Serializable
@Serializable
data class GenreDTO (
    val idGenre: String,
    val name: String
)