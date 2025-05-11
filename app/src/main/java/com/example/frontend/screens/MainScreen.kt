package com.example.frontend.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.frontend.BottomNavItem
import com.example.frontend.screens.Catalog.CatalogScreen
import com.example.frontend.screens.Catalog.GenreBooksScreen
import com.example.frontend.screens.Catalog.SearchScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.MyBooks,
        BottomNavItem.Catalog,
        BottomNavItem.Explore,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF8D6E63), // Светло-коричневый
                            selectedTextColor = Color(0xFF8D6E63),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.MyBooks.route) { MyBooksScreen() }
            composable(BottomNavItem.Catalog.route) { CatalogScreen(navController) }
            composable(BottomNavItem.Explore.route) { ExploreScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }

            composable("search") {
                SearchScreen(navController)
            }
            composable(
                route = "genre/{genre}",
                arguments = listOf(navArgument("genre") { type = NavType.StringType })
            ) { backStackEntry ->
                val genre = backStackEntry.arguments?.getString("genre") ?: ""
                GenreBooksScreen(navController, genre)
            }

        }
    }
}



