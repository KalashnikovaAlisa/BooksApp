package com.example.frontend.screens.Catalog

// Compose
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Для работы с ViewModel и состояния
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.frontend.viewModels.CatalogViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.example.frontend.BookRemote
import com.example.frontend.testUserToken.token

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: CatalogViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    val books by viewModel.books
    val isLoading by viewModel.isLoading
    val hasSearched by viewModel.hasSearched
    val favoriteBookIds by viewModel.favoriteBookIds

    var menuExpandedFor by remember { mutableStateOf<Int?>(null) }
    var pagesDialogFor by remember { mutableStateOf<Int?>(null) }
    var readPages by remember { mutableStateOf("") }

    var selectedBookForReview by remember { mutableStateOf<BookRemote?>(null) }
    var reviewRating by remember { mutableStateOf(3) }
    var reviewComment by remember { mutableStateOf("") }

    var selectedBookForDescription by remember { mutableStateOf<BookRemote?>(null) }

    LaunchedEffect(true) {
        viewModel.loadFavorites(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Введите запрос") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SearchTypeButton("Название", "title", viewModel)
                SearchTypeButton("Автор", "author", viewModel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.searchBooks(query) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Поиск")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                hasSearched && books.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ничего не найдено")
                    }
                }

                else -> {
                    LazyColumn {
                        items(books) { book ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = book.coverUrl,
                                    contentDescription = "Обложка книги",
                                    modifier = Modifier
                                        .size(120.dp) // ещё больше размер
                                        .padding(end = 16.dp)
                                )

                                // Информация о книге — тоже больше
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 2,
                                        modifier = Modifier.clickable { selectedBookForDescription = book }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = book.author,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${book.pages} стр",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Box {
                                    IconButton(onClick = { menuExpandedFor = book.id }) {
                                        val isFavorite = favoriteBookIds.contains(book.id.toString())
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            tint = if (isFavorite) Color.Red else Color.Gray,
                                            contentDescription = "Добавить в избранное"
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = (menuExpandedFor == book.id),
                                        onDismissRequest = { menuExpandedFor = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Хочу прочитать") },
                                            onClick = {
                                                viewModel.addBookToFavorites(book, "Хочу прочитать")
                                                menuExpandedFor = null
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("В процессе") },
                                            onClick = {
                                                pagesDialogFor = book.id
                                                menuExpandedFor = null
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Прочитано") },
                                            onClick = {
                                                //viewModel.addBookToFavorites(book, "Прочитано")
                                                selectedBookForReview = book
                                                menuExpandedFor = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (pagesDialogFor != null) {
                AlertDialog(
                    onDismissRequest = {
                        pagesDialogFor = null
                        readPages = ""
                    },
                    title = { Text("Сколько страниц прочитано?") },
                    text = {
                        OutlinedTextField(
                            value = readPages,
                            onValueChange = { readPages = it },
                            label = { Text("Стр") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val pages = readPages.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            val book = books.firstOrNull { it.id == pagesDialogFor }

                            if (book != null) {
                                viewModel.addBookToFavorites(book, "В процессе", pages)
                            }

                            pagesDialogFor = null
                            readPages = ""
                        }) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            pagesDialogFor = null
                            readPages = ""
                        }) {
                            Text("Отмена")
                        }
                    }
                )
            }
            if (selectedBookForReview != null) {
                AlertDialog(
                    onDismissRequest = {
                        selectedBookForReview = null
                        reviewRating = 3
                        reviewComment = ""
                    },
                    title = { Text("Оставьте отзыв") },
                    text = {
                        Column {
                            Text("Оценка:")
                            Slider(
                                value = reviewRating.toFloat(),
                                onValueChange = { reviewRating = it.toInt() },
                                valueRange = 1f..5f,
                                steps = 3
                            )
                            Text("Комментарий:")
                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                placeholder = { Text("Введите комментарий...") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedBookForReview?.let { book ->
                                viewModel.addBookToFavorites(book, "Прочитано", rating = reviewRating, comment = reviewComment)
                            }
                            selectedBookForReview = null
                            reviewRating = 3
                            reviewComment = ""
                        }) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            selectedBookForReview = null
                            reviewRating = 3
                            reviewComment = ""
                        }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            if (selectedBookForDescription != null) {
                AlertDialog(
                    onDismissRequest = { selectedBookForDescription = null },
                    title = {
                        Text(
                            text = selectedBookForDescription!!.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = selectedBookForDescription!!.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedBookForDescription = null }) {
                            Text("Закрыть")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchTypeButton(label: String, type: String, viewModel: CatalogViewModel) {
    val isSelected = viewModel.searchType == type.lowercase()
    Button(
        onClick = { viewModel.searchType = type  },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFD2B48C) else Color.LightGray
        )
    ) {
        Text(label)
    }
}
