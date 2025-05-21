package com.example.frontend.viewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.ApiClient
import com.example.frontend.BookRemote
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import com.example.frontend.InProgressRequest
import com.example.frontend.MarkAsReadRequest
import com.example.frontend.SelectedByStatusRequest
import com.example.frontend.ToReadRequest
import com.example.frontend.testUserToken

class CatalogViewModel : ViewModel() {
    private val _books = mutableStateOf<List<BookRemote>>(emptyList())
    val books: State<List<BookRemote>> = _books

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _hasSearched = mutableStateOf(false)
    val hasSearched: State<Boolean> = _hasSearched

    var searchType by mutableStateOf("title")
    var searchPerformed by mutableStateOf(false)

    private val token = testUserToken.token

    private val _favoriteBookIds = mutableStateOf<Set<String>>(emptySet())
    val favoriteBookIds: State<Set<String>> = _favoriteBookIds

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _books.value = emptyList()
            _hasSearched.value = true
            return
        }

        _isLoading.value = true
        _hasSearched.value = true

        viewModelScope.launch {
            try {
                _books.value = when (searchType) {
                    "title" -> ApiClient.apiService.searchByTitle(query)
                    "author" -> ApiClient.apiService.searchByAuthor(query)
                    else -> emptyList()
                }
                searchPerformed = true
            } catch (e: Exception) {
                Log.e("Search", "Ошибка при поиске: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBooksByGenre(genre: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _books.value = ApiClient.apiService.getBooksByGenre(genre)
            } catch (e: Exception) {
                Log.e("Genre", "Ошибка при загрузке книг по жанру: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBookToFavorites(book: BookRemote, status: String, pages: Int? = null, rating: Int? = null,
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

                // Обновл список книг после добавления
                loadFavorites(token)

            } catch (e: Exception) {
                Log.e("MyBooks", "Ошибка при добавлении книги: ${e.message}")
            }
        }
    }

    fun loadFavorites(token: String) {
        viewModelScope.launch {
            try {
                val statuses = listOf("Хочу прочитать", "В процессе", "Прочитано")
                val allBooks = mutableListOf<BookRemote>()

                for (status in statuses) {
                    val books = ApiClient.apiService.getBooksByStatus(
                        SelectedByStatusRequest(token, status)
                    )
                    allBooks.addAll(books)
                }

                _favoriteBookIds.value = allBooks.map { it.id.toString() }.toSet()
            } catch (e: Exception) {
                Log.e("Favorites", "Ошибка при загрузке избранных книг: ${e.message}")
            }
        }
    }

}

