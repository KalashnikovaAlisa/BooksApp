package ru.database.statuses
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object Statuses : IdTable<Int>("statuses") {
    override val id: Column<EntityID<Int>> = integer("id_status").autoIncrement().entityId()
    val name = varchar("name", 25)

    override val primaryKey = PrimaryKey(id)
}

