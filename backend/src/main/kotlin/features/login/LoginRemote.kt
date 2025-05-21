package ru.features.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginReceiveRemote(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponseRemote(
    //будем возращать уникальный ключ клиента, кот будет исп когда нужно узнать клиента по его токену
    val token: String
)

