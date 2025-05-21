package ru.features.book
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteAddRequest(
    val token: String,
    val bookId: String,
    val status: String
)

@Serializable
data class FavoriteUpdateRequest(
    val token: String,
    val bookId: String,
    val newStatus: String
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
@Serializable
data class ToReadRequest(
    val token: String,
    val bookId: String
)
/*
@Serializable
data class MarkAsReadRequest(
    val token: String,
    val bookId: String
)*/
@Serializable
data class MarkAsRead(
    val token: String,
    val bookId: String,
    val rating: Int,
    val comment: String
)

@Serializable
data class ReadingStatResponse(
    val date: String,
    val readPages: Int
)
@Serializable
data class StatsRequest(val token: String, val bookId: String)

@Serializable
data class ReviewRequest(
    val token: String,
    val bookId: String
)
@Serializable
data class ReviewDto(
    val comment: String,
    val date: String
)

