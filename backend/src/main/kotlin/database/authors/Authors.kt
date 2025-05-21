package ru.database.authors

import org.jetbrains.exposed.sql.Table

object Authors: Table() {
    val idAuthor = varchar("id_author", 50)
    val name = varchar("name", 50)
}