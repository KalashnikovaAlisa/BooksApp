package ru.database.users

import org.jetbrains.exposed.dao.id.EntityID

//data transfer object
class UserDTO (
    val id_user: EntityID<Int>,
    val login: String,
    val password: String
)