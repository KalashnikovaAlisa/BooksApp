package com.example.frontend.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.frontend.ApiClient
import com.example.frontend.RegisterRequest
import com.example.frontend.testUserToken
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel : ViewModel() {
    var login by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf("")

    fun performRegistration(navController: NavController) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.register(RegisterRequest(login, password))
                error = ""
                testUserToken.token = response.token
                navController.navigate("main") {
                    popUpTo("register") { inclusive = true }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                error = errorBody ?: "Ошибка регистрации: ${e.message}"
            } catch (e: Exception) {
                error = "Сетевая ошибка: ${e.localizedMessage}"
            }
        }
    }
}