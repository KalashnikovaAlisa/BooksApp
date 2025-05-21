package ru.database.books

import org.jetbrains.exposed.sql.insert
import ru.database.tokens.TokenDTO

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ru.database.authors.Authors
import ru.database.ganres.Genres
import ru.database.users.UserDTO

object Books: Table() {
    val idBook = varchar("id_book", 50)
    val title = varchar("title", 50)
    val authorId = reference("id_author", Authors.idAuthor)
    val genreId = reference("id_genre", Genres.idGenre)
    val pages = integer("pages")
    val coverUrl = varchar("cover_url", 255)
    val description = varchar("description", 1000)
    override val primaryKey = PrimaryKey(idBook)
}