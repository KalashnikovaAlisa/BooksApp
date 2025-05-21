package com.example.frontend.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.example.frontend.ApiClient
import com.example.frontend.BookRemote
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.frontend.FavoriteAddRequest
import com.example.frontend.FavoriteDeleteRequest
import com.example.frontend.InProgressRequest
import com.example.frontend.MarkAsReadRequest
import com.example.frontend.ReadingStatResponse
import com.example.frontend.ReviewDto
import com.example.frontend.ReviewRequest
import com.example.frontend.SelectedByStatusRequest
import com.example.frontend.StatsRequest
import com.example.frontend.ToReadRequest
import com.example.frontend.testUserToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyBooksViewModel : ViewModel() {
    private val _books = mutableStateOf<List<BookRemote>>(emptyList())
    val books: State<List<BookRemote>> = _books

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    var selectedStatus by mutableStateOf("Хочу прочитать")


    private val token = testUserToken.token;

    val readingStats = mutableStateOf<List<ReadingStatResponse>>(emptyList())

    fun loadBooksByStatus(status: String) {
        selectedStatus = status
        _isLoading.value = true
        _books.value = emptyList()

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getBooksByStatus(
                    SelectedByStatusRequest(token = token, status = status)
                )
                //_books.value = response
                _books.value = response.toList()

            } catch (e: Exception) {
                Log.e("MyBooks", "Ошибка загрузки книг: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    val reviews = mutableStateOf<List<ReviewDto>>(emptyList())

    fun loadReviews(bookId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getReviewsForBook(
                    ReviewRequest(token, bookId)
                )
                reviews.value = response

            } catch (e: Exception) {
                Log.e("MyBooks", "Ошибка загрузки отзывов: ${e.message}")
            }
        }
    }



    fun addBookToFavorites(book: BookRemote, status: String, pages: Int? = null,
                           rating: Int? = null,
                           comment: String? = null) {
        viewModelScope.launch {
            try {
                when (status) {
                    "Хочу прочитать" -> {
                        ApiClient.apiService.markBookToRead(
                            ToReadRequest(token, book.id.toString())
                        )
                    }

                    "В процессе" -> {
                        if (pages != null) {
                            ApiClient.apiService.markBookInProgress(
                                InProgressRequest(token, book.id.toString(), pages)
                            )
                        } else {
                            Log.e("MyBooks", "Не указано количество прочитанных страниц")
                        }
                    }

                    "Прочитано" -> {
                        if (rating != null && comment != null) {
                            ApiClient.apiService.markBookAsRead(
                                MarkAsReadRequest(
                                    token,
                                    book.id.toString(),
                                    rating,
                                    comment
                                )
                            )
                        } else {
                            Log.e(
                                "MyBooks",
                                "Оценка и комментарий обязательны для статуса 'Прочитано'"
                            )
                        }
                    }
                }

                // Обновляем список книг после добавления
                loadBooksByStatus(selectedStatus)

            } catch (e: Exception) {
                Log.e("MyBooks", "Ошибка при добавлении книги: ${e.message}")
            }
        }
    }
    fun removeBook(book: BookRemote,status: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                ApiClient.apiService.removeBookFromFavorites(
                    FavoriteDeleteRequest(token, book.id.toString())
                )

                // Обновить список после удаления
                loadBooksByStatus(status)
                onSuccess("Книга удалена из \"$status\"")
            } catch (e: Exception) {
                Log.e("MyBooks", "Ошибка при удалении книги: ${e.message}")
            }
        }
    }
    fun loadReadingStats(bookId: String) {
        viewModelScope.launch {
            try {
                val result = ApiClient.apiService.getReadingStats(
                    StatsRequest(token = token, bookId = bookId)
                )
                readingStats.value = result
            } catch (e: Exception) {
                Log.e("Stats", "Ошибка загрузки истории: ${e.message}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(dateStr: String): String {
        return try {
            val formatterInput = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val formatterOutput = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

            val dateTime = LocalDateTime.parse(dateStr, formatterInput)
            dateTime.format(formatterOutput)
        } catch (e: Exception) {
            dateStr // fallback
        }
    }
}
