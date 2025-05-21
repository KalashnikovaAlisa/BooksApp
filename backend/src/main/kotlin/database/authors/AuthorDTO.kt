package ru.database.authors

import kotlinx.serialization.Serializable

@Serializable
data class AuthorDTO (
    var authorId: String,
    var name: String
)