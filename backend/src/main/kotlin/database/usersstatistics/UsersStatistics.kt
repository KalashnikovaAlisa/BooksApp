package ru.database.usersstatistics

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import ru.database.selectedbooks.SelectedBooks
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object UsersStatistics : IdTable<Int>("users_statistics") {
    override val id: Column<EntityID<Int>> = integer("id_entry").autoIncrement().entityId()
    val date = datetime("date")
    val idSelectedBook = reference("id_selected_book", SelectedBooks)
    val readPages = integer("read_pages")
    override val primaryKey = PrimaryKey(id)
}
