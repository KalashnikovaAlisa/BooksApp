package com.example.frontend

import kotlinx.serialization.Serializable

data class LoginRequest(val login: String, val password: String)
data class LoginResponse(val token: String)

data class RegisterRequest(val login: String, val password: String)
data class RegisterResponse(val token: String)

data class ErrorResponse(
    val message: String
)

@Serializable
data class FavoriteAddRequestOld(
    val token: String,
    val bookId: String,
    val status: String // "Хочу прочитать", "В процессе", "Прочитано"
)

@Serializable
data class FavoriteUpdateRequest(
    val token: String,
    val bookId: String,
    val newStatus: String
)
@Serializable
data class ToReadRequest(
    val token: String,
    val bookId: String
)
@Serializable
data class MarkAsReadRequest(
    val token: String,
    val bookId: String,
    val rating: Int,
    val comment: String
)


@Serializable
data class FavoriteDeleteRequest(
    val token: String,
    val bookId: String
)

@Serializable
data class SelectedByStatusRequest(
    val token: String,
    val status: String
)

@Serializable
data class InProgressRequest(
    val token: String,
    val bookId: String,
    val readPages: Int
)

data class FavoriteAddRequest(
    val bookId: Int,
    val status: String,
    val pagesRead: Int? = null
)

@Serializable
data class StatsRequest(
    val token: String,
    val bookId: String
)

@Serializable
data class ReadingStatResponse(
    val date: String,
    val readPages: Int
)

@Serializable
data class ReviewDto(val comment: String, val date: String)

@Serializable
data class ReviewRequest(
    val token: String,
    val bookId: String
)

