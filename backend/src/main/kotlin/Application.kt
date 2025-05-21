package ru

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import ru.features.login.configureLoginRouting
import ru.features.register.configureRegisterRouting
import ru.features.book.configureBookRouting

fun main() {
    Database.connect(url = "jdbc:postgresql://localhost:5432/bookapp", driver = "org.postgresql.Driver",
        user = "postgres", password = "123")

    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureBookRouting()
    configureSerialization()
}
