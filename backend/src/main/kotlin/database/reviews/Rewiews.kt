package ru.database.reviews

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import ru.database.selectedbooks.SelectedBooks

object Reviews : Table("reviews") {
    val id = integer("id_view").autoIncrement().uniqueIndex()
    val date = date("date_view")
    //val idSelectedBook = integer("id_selected_book").references(SelectedBooks.id)
    val idSelectedBook = reference("id_selected_book", SelectedBooks)
    val rating = integer("rating")
    val comment = varchar("comment", 200)

    override val primaryKey = PrimaryKey(id)
}