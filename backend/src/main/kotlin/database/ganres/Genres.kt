package ru.database.ganres
import org.jetbrains.exposed.sql.Table

object Genres : Table() {
    val idGenre = varchar("id_genre", 50)
    val name = varchar("name", 50)
    //override val primaryKey = PrimaryKey(idGenre)
}