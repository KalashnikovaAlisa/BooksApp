package ru.features.login

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import ru.database.tokens.Tokens
import ru.database.users.Users
import java.util.UUID

class LoginController (private val call: ApplicationCall) {
    suspend fun performLogin(){
        val receive = call.receive(LoginReceiveRemote::class)
        val userDTO = Users.fetchUser(receive.login)

        if(userDTO == null){
            call.respond(
                HttpStatusCode.NotFound,
                "Пользователь не найден")
        } else{
            if(userDTO.password == receive.password){
                val token = UUID.randomUUID().toString()
                Tokens.insert(userDTO.id_user, token)
                //возврат об успешной регистрации
                call.respond(LoginResponseRemote(token = token))
            } else{
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Неверный логин или пароль")
            }
        }
    }
}