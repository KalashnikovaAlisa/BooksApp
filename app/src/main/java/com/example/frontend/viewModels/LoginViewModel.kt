package com.example.frontend.viewModels

// LoginViewModel.kt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.frontend.ApiClient
import com.example.frontend.LoginRequest
import com.example.frontend.testUserToken
import kotlinx.coroutines.launch
import retrofit2.HttpException


class LoginViewModel : ViewModel() {

    var login by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf("")

    fun performLogin(navController: NavController) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(login, password))
                testUserToken.token = response.token
                error = ""
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                error = errorBody ?: "Ошибка входа: ${e.message}"
            } catch (e: Exception) {
                error = "Сетевая ошибка: ${e.localizedMessage}"
            }
        }
    }
}
