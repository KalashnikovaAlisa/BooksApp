package com.example.frontend.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
// Compose
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.example.frontend.BookRemote
import com.example.frontend.viewModels.MyBooksViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(viewModel: MyBooksViewModel = viewModel()) {
    val selectedTab = remember { mutableStateOf("Хочу прочитать") }
    val books by viewModel.books
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val currentToastMessage by rememberUpdatedState(toastMessage)
    var selectedBookForStatusChange by remember { mutableStateOf<BookRemote?>(null) }
    var pagesInput by remember { mutableStateOf("") }

    var selectedBookForStats by remember { mutableStateOf<BookRemote?>(null) }

    var selectedBookForReview by remember { mutableStateOf<BookRemote?>(null) }
    var ratingInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }

    var selectedBookForReviews by remember { mutableStateOf<BookRemote?>(null) }

    var selectedBookForDescription by remember { mutableStateOf<BookRemote?>(null) }

    LaunchedEffect(selectedTab.value) {
        viewModel.loadBooksByStatus(selectedTab.value)
    }

    LaunchedEffect(toastMessage) {
        currentToastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            delay(500)
            toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мои книги") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Хочу прочитать", "В процессе", "Прочитано").forEach { status ->
                    TextButton(onClick = {
                        selectedTab.value = status
                        viewModel.loadBooksByStatus(status)
                    }) {
                        Text(
                            text = status,
                            color = if (selectedTab.value == status) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (books.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет добавленных книг")
                }
            } else {
                LazyColumn {
                    items(books, key = { it.id }) { book ->
                        BookItem(
                            book = book,
                            status = selectedTab.value,
                            onAddClick = { status ->
                                if (status == "В процессе") {
                                    selectedBookForStatusChange = book
                                } else if (status == "Удалить") {
                                    viewModel.removeBook(book, selectedTab.value) { message ->
                                        toastMessage = message
                                    }
                                } else if (status == "Прочитано") {
                                    selectedBookForReview = book
                                } else {
                                    viewModel.addBookToFavorites(book, status)
                                }
                            },
                            onShowStats = { book ->
                                if (selectedTab.value == "В процессе") {
                                    selectedBookForStats = book
                                    viewModel.loadReadingStats(book.id.toString())
                                } else if (selectedTab.value == "Прочитано") {
                                    selectedBookForReviews = book
                                    viewModel.loadReviews(book.id.toString())
                                }
                            },
                            onReviewClick = { book ->
                                selectedBookForReview = book
                            },
                            onShowDescription = { selectedBookForDescription = it }
                        )

                    }
                }
            }
        }
    }

    //при нажатии на "глаз"
    selectedBookForStats?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBookForStats = null },
            title = { Text("История прочтения") },
            text = {
                val stats = viewModel.readingStats.value
                if (stats.isEmpty()) {
                    Text("Нет данных о прочитанных страницах.")
                } else {
                    Column {
                        stats.forEach {
                            Text(
                                text = "${viewModel.formatDate(it.date)}: ${it.readPages} стр.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookForStats = null }) {
                    Text("Закрыть")
                }
            }
        )
    }


    // Диалог "В процессе"
    selectedBookForStatusChange?.let { book ->
        AlertDialog(
            onDismissRequest = {
                selectedBookForStatusChange = null
                pagesInput = ""
            },
            title = { Text("Сколько страниц прочитано?") },
            text = {
                OutlinedTextField(
                    value = pagesInput,
                    onValueChange = { pagesInput = it },
                    label = { Text("Стр") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val pages = pagesInput.toIntOrNull()
                    when {
                        pages == null || pages <= 0 -> {
                            Toast.makeText(context, "Некорректный ввод", Toast.LENGTH_SHORT).show()
                        }
                        pages > book.pages -> {
                            Toast.makeText(context, "Слишком много страниц", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            viewModel.addBookToFavorites(book, "В процессе", pages)
                            selectedBookForStatusChange = null
                            pagesInput = ""
                        }
                    }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedBookForStatusChange = null
                    pagesInput = ""
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог "Прочитано" — отзыв
    selectedBookForReview?.let { book ->
        AlertDialog(
            onDismissRequest = {
                selectedBookForReview = null
                ratingInput = ""
                commentInput = ""
            },
            title = { Text("Оцените книгу и оставьте отзыв") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ratingInput,
                        onValueChange = { ratingInput = it },
                        label = { Text("Оценка (1–5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        label = { Text("Комментарий") },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val rating = ratingInput.toIntOrNull()
                    when {
                        rating == null || rating !in 1..5 -> {
                            Toast.makeText(context, "Оценка должна быть от 1 до 5", Toast.LENGTH_SHORT).show()
                        }
                        commentInput.isBlank() -> {
                            Toast.makeText(context, "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            viewModel.addBookToFavorites(
                                book,
                                "Прочитано",
                                rating = rating,
                                comment = commentInput
                            )
                            selectedBookForReview = null
                            ratingInput = ""
                            commentInput = ""
                        }
                    }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedBookForReview = null
                    ratingInput = ""
                    commentInput = ""
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    //диалог показ отзывов
    selectedBookForReviews?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBookForReviews = null },
            title = { Text("Отзывы о книге") },
            text = {
                val reviewList = viewModel.reviews.value
                if (reviewList.isEmpty()) {
                    Text("Отзывов пока нет.")
                } else {
                    Column {
                        reviewList.forEach {
                            Text(
                                text = "Дата: ${it.date}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = it.comment,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookForReviews = null }) {
                    Text("Закрыть")
                }
            }
        )
    }

    //диалог описание
    selectedBookForDescription?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBookForDescription = null },
            title = { Text("Описание книги", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(book.description, style = MaterialTheme.typography.bodyLarge)
            },
            confirmButton = {
                TextButton(onClick = { selectedBookForDescription = null }) {
                    Text("Закрыть")
                }
            }
        )
    }

}


@Composable
fun BookItem(
    book: BookRemote,
    status: String,
    onAddClick: (String) -> Unit,
    onShowStats: (BookRemote) -> Unit, //статистика
    onReviewClick: (BookRemote) -> Unit, //отзывы
    onShowDescription: (BookRemote) -> Unit // описание
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                modifier = Modifier.clickable { onShowDescription(book) }
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

        //пентограмма глаз если статус в процессе или прочитано
        if (status == "В процессе" || status == "Прочитано") {
            IconButton(onClick = { onShowStats(book) }) {
                Icon(Icons.Default.RemoveRedEye, contentDescription = if (status == "Прочитано") "Отзывы" else "История")
            }
        }


        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Изменить статус")
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Хочу прочитать") },
                    onClick = {
                        showMenu = false
                        onAddClick("Хочу прочитать")
                    }
                )
                DropdownMenuItem(
                    text = { Text("В процессе") },
                    onClick = {
                        showMenu = false
                        onAddClick("В процессе")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Прочитано") },
                    onClick = {
                        showMenu = false
                        //onAddClick("Прочитано")
                        onReviewClick(book)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        showMenu = false
                        onAddClick("Удалить")
                    }
                )
            }
        }
    }
}