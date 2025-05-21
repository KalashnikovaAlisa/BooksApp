package ru.features.book
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import ru.database.books.Books

fun Application.configureBookRouting() {
    routing {
        get("/books/author/{name}") {
            println(call.parameters["name"])
            val authorName = call.parameters["name"] ?: return@get call.respondText("Missing author name", status = HttpStatusCode.BadRequest)
            val books = BookController.searchByAuthor(authorName)
            call.respond(books)
        }

        get("/books/genre/{name}") {
            val genreName = call.parameters["name"] ?: return@get call.respondText("Missing genre name", status = HttpStatusCode.BadRequest)
            val books = BookController.searchByGenre(genreName)
            call.respond(books)
        }

        get("/books/title/{title}") {
            val title = call.parameters["title"] ?: return@get call.respondText("Missing title", status = HttpStatusCode.BadRequest)
            val books = BookController.searchByTitle(title)
            call.respond(books)
        }
        // Добавить книгу в избранное
        post("/favorites/add") {
            val request = call.receive<FavoriteAddRequest>()
            try {
                BookController.addBookToFavorites(request.token, request.bookId, request.status)
                call.respond(HttpStatusCode.OK, "Book added to favorites with status ${request.status}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Изменить статус избранной книги
        post("/favorites/update-status") {
            val request = call.receive<FavoriteUpdateRequest>()
            try {
                BookController.updateBookStatus(request.token, request.bookId, request.newStatus)
                call.respond(HttpStatusCode.OK, "Book status updated to ${request.newStatus}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Удалить книгу из избранного
        delete("/favorites/delete") {
            val request = call.receive<FavoriteDeleteRequest>()
            try {
                BookController.removeBookFromFavorites(request.token, request.bookId)
                call.respond(HttpStatusCode.OK, "Book removed from favorites")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        post("/favorites/by-status") {
            //val params = call.receiveParameters()
            val request = call.receive<SelectedByStatusRequest>()
            val token = request.token
            val status = request.status

            try {
                val books = BookController.getBooksByStatus(token, status)
                call.respond(books)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
        post("/reviews/get") {
            val request = call.receive<ReviewRequest>()
            val token = request.token
            val bookId = request.bookId

            try {
                val reviews = BookController.getReviewsForBook(token, bookId)
                call.respond(reviews)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        post("/favorites/to-read") {
            val request = call.receive<ToReadRequest>()
            try {
                BookController.markBookToRead(request.token, request.bookId)
                call.respond(HttpStatusCode.OK, "Book marked as 'to read'")
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        post("/favorites/in-progress") {
            val request = call.receive<InProgressRequest>()
            try {
                BookController.markBookInProgressNew(request.token, request.bookId, request.readPages)
                call.respond(HttpStatusCode.OK, "Book marked as 'in progress' and statistics recorded")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        post("/favorites/read") {
            val request = call.receive<MarkAsRead>()
            try {
                BookController.markBookAsRead(
                    request.token, request.bookId, request.rating, request.comment
                )
                call.respond(HttpStatusCode.OK, "Book marked as 'read' with review")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        post("/favorites/in-progress/stats") {
            val request = call.receive<StatsRequest>()
            try {
                val stats = BookController.getReadingStats(request.token, request.bookId)
                call.respond(stats)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Ошибка")
            }
        }

    }
}
