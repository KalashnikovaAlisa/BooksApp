package ru.database.tokens

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ru.database.users.UserDTO
import ru.database.users.Users
import java.util.UUID

object Tokens: Table() {
    val id_token = varchar("id_token", 50).default(UUID.randomUUID().toString())
    val id_user = reference("id_user", Users.id) // FK
    val token = varchar("token", 50).uniqueIndex()
    override val primaryKey = PrimaryKey(id_token)

    fun insert(userId: EntityID<Int>, token: String) {
        transaction {
            Tokens.insert {
                it[id_token] = UUID.randomUUID().toString()
                it[id_user] = userId
                it[Tokens.token] = token
            }
        }
    }
}