package ru.database.selectedbooks
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import ru.database.books.Books
import ru.database.statuses.Statuses
import ru.database.users.Users

object SelectedBooks : IdTable<Int>("selected_books") {
    override val id: Column<EntityID<Int>> = integer("id_selected_book").autoIncrement().entityId()
    val idUser = reference("id_user", Users.id)
    val idBook = varchar("id_book", 50).references(Books.idBook)
    val idStatus = reference("id_status", Statuses.id)
}