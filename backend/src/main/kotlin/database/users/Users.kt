package ru.database.users
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users: IdTable<Int>() {
    override val id: Column<EntityID<Int>> = integer("id_user").autoIncrement().entityId()
    val login = varchar("login", 50).uniqueIndex()
    val password = varchar("password", 25)

    override val primaryKey = PrimaryKey(id)

    fun insert(userDTO: UserDTO): EntityID<Int> {
        return transaction{
            Users.insertAndGetId {
                it[login] = userDTO.login
                it[password] = userDTO.password
            }
        }
    }

    //получение пользователя
    fun fetchUser(login: String) : UserDTO? {
        return try{
            transaction {
                val userModel = Users.select { Users.login.eq(login) }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        id_user = it[Users.id],  // Возвращаем EntityID<Int>
                        login = it[Users.login],
                        password = it[Users.password]
                    )
                }
            }
        } catch(e: Exception){
            null
        }

    }

}