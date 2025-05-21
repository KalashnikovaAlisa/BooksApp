package com.example.frontend

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object MyBooks : BottomNavItem("my_books", Icons.Default.Book, "Мои книги")
    object Catalog : BottomNavItem("catalog", Icons.AutoMirrored.Filled.MenuBook, "Каталог")
    object Explore : BottomNavItem("explorer", Icons.Default.Star, "Интересное")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Профиль")
}
