package ru

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


fun Application.configureRouting() {
    routing {
        get("/login") {
            call.respondText("Hello, world!")
        }
    }
}
