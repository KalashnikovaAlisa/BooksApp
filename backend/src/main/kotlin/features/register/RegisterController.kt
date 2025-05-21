package ru.features.register

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import ru.database.tokens.TokenDTO
import ru.database.tokens.Tokens
import ru.database.users.UserDTO
import ru.database.users.Users
import java.util.UUID

class RegisterController(private val call: ApplicationCall) {

    suspend fun registerNewUser() {
        val registerReceiveRemote = call.receive<RegisterRecieveRemote>()

        // Проверка — существует ли пользователь с таким логином
        val existingUser = Users.fetchUser(registerReceiveRemote.login)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return
        }

        val token = UUID.randomUUID().toString()

        try {
            // Добавляем пользователя и получаем его id
            val idUser = transaction {
                Users.insertAndGetId {
                    it[login] = registerReceiveRemote.login
                    it[password] = registerReceiveRemote.password
                }// Получаем EntityID
            }
            // Добавляем токен в таблицу Tokens
            transaction {
                Tokens.insert {
                    it[Tokens.id_user] = idUser
                    it[Tokens.token] = token
                }
            }

            // Отправляем токен клиенту
            call.respond(RegisterResponseRemote(token = token))
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "User already exists or database error")
        }
    }
}