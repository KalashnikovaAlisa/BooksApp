package com.example.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.example.frontend.ui.theme.FrontendTheme

import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend.screens.Catalog.CatalogScreen
import com.example.frontend.screens.Catalog.GenreBooksScreen
import com.example.frontend.screens.Catalog.SearchScreen
import com.example.frontend.screens.Auth.LoginScreen
import com.example.frontend.screens.MainScreen
import com.example.frontend.screens.Auth.RegisterScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            FrontendTheme{
                //MainScreen()
                AppNavigation()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FrontendTheme {
        Greeting("Android")
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register_screen") { RegisterScreen(navController) }
        composable("main") { MainScreen() }
        composable("catalog") { CatalogScreen(navController) }
        composable("search") { SearchScreen(navController) }
        composable("genre/{name}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            GenreBooksScreen(navController, genre = name)
        }
    }
}
