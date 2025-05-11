package com.example.frontend

// ApiService.kt
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("/books/genre/{name}")
    suspend fun getBooksByGenre(@Path("name") name: String): List<BookRemote>

    @GET("/books/title/{title}")
    suspend fun searchByTitle(@Path("title") title: String): List<BookRemote>

    @GET("/books/author/{name}")
    suspend fun searchByAuthor(@Path("name") author: String): List<BookRemote>

    // Добавить книгу в избранное (с указанием статуса: "Хочу прочитать", "В процессе", "Прочитано")
    @POST("/favorites/add")
    suspend fun addBookToFavorites(@Body request: FavoriteAddRequest): String

    // Изменить статус книги в избранном
    @POST("/favorites/update-status")
    suspend fun updateBookStatus(@Body request: FavoriteUpdateRequest): String

    // Удалить книгу из избранного
    @HTTP(method = "DELETE", path = "/favorites/delete", hasBody = true)
    suspend fun removeBookFromFavorites(@Body request: FavoriteDeleteRequest): String

    // Получить книги по статусу (например: "Хочу прочитать")
    @POST("/favorites/by-status")
    suspend fun getBooksByStatus(@Body request: SelectedByStatusRequest): List<BookRemote>

    // Отметить как "в процессе" с количеством прочитанных страниц
    @POST("/favorites/in-progress")
    suspend fun markBookInProgress(@Body request: InProgressRequest): String
    @POST("/favorites/to-read")
    suspend fun markBookToRead(@Body request: ToReadRequest)

    @POST("/favorites/in-progress/stats")
    suspend fun getReadingStats(@Body request: StatsRequest): List<ReadingStatResponse>

    @POST("/favorites/read")
    suspend fun markBookAsRead(@Body request: MarkAsReadRequest): String

    @POST("reviews/get")
    suspend fun getReviewsForBook(@Body request: ReviewRequest): List<ReviewDto>

}