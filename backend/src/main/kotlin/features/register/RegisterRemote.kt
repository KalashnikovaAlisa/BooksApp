package ru.features.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRecieveRemote(
    val login: String,
    val password: String
)

@Serializable
data class RegisterResponseRemote(
    val token: String
)
