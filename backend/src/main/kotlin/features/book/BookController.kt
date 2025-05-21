package ru.features.book

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upperCase
import ru.database.authors.Authors
import ru.database.books.Books
import ru.database.ganres.Genres
import ru.database.tokens.Tokens
import ru.database.statuses.Statuses
import ru.database.selectedbooks.SelectedBooks
import ru.database.usersstatistics.UsersStatistics
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ru.database.reviews.Reviews
import java.time.LocalDate


object BookController {
    fun searchByAuthor(authorName: String): List<BookRemote> = transaction {
        (Books innerJoin Authors innerJoin Genres).select {
            Authors.name.upperCase() like "%${authorName.uppercase()}%"
        }.map { toBookRemote(it) }
    }

    fun searchByGenre(genreName: String): List<BookRemote> = transaction {
        (Books innerJoin Authors innerJoin Genres).select {
            Genres.name.upperCase() like "%${genreName.uppercase()}%"

        }.map { toBookRemote(it) }
    }

    fun searchByTitle(title: String): List<BookRemote> = transaction {
        (Books innerJoin Authors innerJoin Genres).select {
            //Books.title.upperCase() like "%${title.uppercase()}%"
            LowerCase(Books.title) like "%${title.lowercase()}%"
        }.map { toBookRemote(it) }
    }

    private fun toBookRemote(row: ResultRow): BookRemote {
        return BookRemote(
            id = row[Books.idBook],
            title = row[Books.title],
            author = row[Authors.name],
            genre = row[Genres.name],
            pages = row[Books.pages],
            coverUrl = row[Books.coverUrl],
            description = row[Books.description]
        )
    }

    fun getUserIdByToken(token: String): EntityID<Int>? {
        return try {
            transaction {
                Tokens.select { Tokens.token eq token }
                    .map { it[Tokens.id_user] }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
    fun addBookToFavorites(token: String, bookId: String, statusName: String) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction
        val statusId = Statuses.select { Statuses.name eq statusName }.single()[Statuses.id]

        // Проверяем, существует ли запись с таким user и book
        val existing = SelectedBooks.select {
            (SelectedBooks.idUser eq userId) and (SelectedBooks.idBook eq bookId)
        }.singleOrNull()

        val selectedBookId: EntityID<Int> = when {
            existing == null -> {
                // Записи нет — создаём
                SelectedBooks.insertAndGetId {
                    it[idUser] = userId
                    it[idBook] = bookId
                    it[idStatus] = statusId
                }
            }

            existing[SelectedBooks.idStatus] != statusId -> {
                // Статус отличается — обновляем
                val id = existing[SelectedBooks.id]
                SelectedBooks.update({ SelectedBooks.id eq id }) {
                    it[idStatus] = statusId
                }
                existing[SelectedBooks.id]

            }

            else -> {
                // Уже существует с таким же статусом — ничего не делаем
                return@transaction
            }
        }

        if (statusName == "В процессе") {
            UsersStatistics.insert {
                it[date] = java.time.LocalDateTime.now()
                it[idSelectedBook] = selectedBookId
                it[readPages] = 0
            }
        }
    }


    fun updateBookStatus(token: String, bookId: String, newStatusName: String) = transaction {
        val userId = getUserIdByToken(token)?: return@transaction
        val newStatusId = Statuses.select { Statuses.name eq newStatusName }.single()[Statuses.id]
        val selectedBook = SelectedBooks.select {
            (SelectedBooks.idUser eq userId) and (SelectedBooks.idBook eq bookId)
        }.single()

        val selectedBookId = selectedBook[SelectedBooks.id]
        val currentStatusId = selectedBook[SelectedBooks.idStatus]

        val currentStatusName = Statuses.select { Statuses.id eq currentStatusId }.single()[Statuses.name]

        // Обновляем статус
        SelectedBooks.update({ SelectedBooks.id eq selectedBookId }) {
            it[idStatus] = newStatusId
        }

        // Переход на "в процессе"
        if (newStatusName == "В процессе" && currentStatusName != "В процессе") {
            UsersStatistics.insert {
                //it[date] = LocalDate.now()
                it[date] = java.time.LocalDateTime.now()
                it[idSelectedBook] = selectedBookId
                it[readPages] = 0
            }
        }

        // Переход на "закончено"
        if (newStatusName == "Прочитано" && currentStatusName == "В процессе") {
            UsersStatistics.deleteWhere { UsersStatistics.idSelectedBook eq selectedBookId }
        }
    }

    fun removeBookFromFavorites(token: String, bookId: String) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction
        val selectedBook = SelectedBooks.select {
            (SelectedBooks.idUser eq userId) and (SelectedBooks.idBook eq bookId)
        }.singleOrNull() ?: return@transaction

        val selectedBookId = selectedBook[SelectedBooks.id]
        val statusId = selectedBook[SelectedBooks.idStatus]
        val statusName = Statuses.select { Statuses.id eq statusId }.single()[Statuses.name]

        if (statusName == "В процессе") {
            UsersStatistics.deleteWhere { UsersStatistics.idSelectedBook eq selectedBookId }
        }
        if (statusName == "Прочитано") {
            Reviews.deleteWhere { Reviews.idSelectedBook eq selectedBookId }
        }

        SelectedBooks.deleteWhere { SelectedBooks.id eq selectedBookId }
    }

    //вывод списка книг по статусу для конкр пользователя
    fun getBooksByStatus(token: String, statusName: String): List<BookRemote> = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction emptyList()
        val statusId = Statuses.select { Statuses.name eq statusName }
            .singleOrNull()?.get(Statuses.id) ?: return@transaction emptyList()

        if (statusName == "Прочитано") {
            val queryResult = (Reviews innerJoin SelectedBooks innerJoin Books innerJoin Authors innerJoin Genres)
                .slice(
                    Books.idBook,
                    Books.title,
                    Authors.name,
                    Genres.name,
                    Books.pages,
                    Books.coverUrl,
                    Books.description)
                .select {
                    (SelectedBooks.idUser eq userId) and
                            (SelectedBooks.id eq Reviews.idSelectedBook) and
                            (SelectedBooks.idBook eq Books.idBook) and
                            (Books.authorId eq Authors.idAuthor) and
                            (Books.genreId eq Genres.idGenre)
                }
                .withDistinct()
                .toList()

            queryResult.map { toBookRemote(it) }
        } else {
            (SelectedBooks innerJoin Books innerJoin Authors innerJoin Genres)
                .select {
                    (SelectedBooks.idUser eq userId) and
                            (SelectedBooks.idStatus eq statusId)
                }
                .map { toBookRemote(it) }
        }

    }
    fun getReviewsForBook(token: String, bookId: String): List<ReviewDto> = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction emptyList()

        (Reviews innerJoin SelectedBooks)
            .select {
                (SelectedBooks.idUser eq userId) and
                        (SelectedBooks.idBook eq bookId) and
                        (Reviews.idSelectedBook eq SelectedBooks.id)
            }
            .map {
                ReviewDto(
                    comment = it[Reviews.comment],
                    date = it[Reviews.date].toString()
                )
            }
    }
    /*
    fun markBookInProgress(token: String, bookId: String, readPages: Int) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction

        // Получаем книгу
        val book = Books.select { Books.idBook eq bookId }.singleOrNull()
            ?: throw IllegalArgumentException("Книга не найдена")

        val totalPages = book[Books.pages]

        // Проверка: переданное значение больше, чем всего страниц
        if (readPages > totalPages) {
            throw IllegalArgumentException("Число прочитанных страниц превышает количество страниц в книге.")
            return@transaction
        }

        val inProgressStatusId = Statuses
            .select { Statuses.name eq "В процессе" }
            .single()[Statuses.id]

        // Проверка: есть ли уже эта книга у пользователя со статусом "в процессе"
        val existing = SelectedBooks.select {
            (SelectedBooks.idUser eq userId) and
                    (SelectedBooks.idBook eq bookId) and
                    (SelectedBooks.idStatus eq inProgressStatusId)
        }.singleOrNull()

        val selectedBookId = if (existing != null) {
            existing[SelectedBooks.id]
        } else {
            // Ищем, есть ли книга у пользователя с другим статусом
            val otherStatus = SelectedBooks.select {
                (SelectedBooks.idUser eq userId) and
                        (SelectedBooks.idBook eq bookId) and
                        (SelectedBooks.idStatus neq inProgressStatusId)
            }.singleOrNull()

            if (otherStatus != null) {
                val id = otherStatus[SelectedBooks.id]
                SelectedBooks.update({ SelectedBooks.id eq id }) {
                    it[idStatus] = inProgressStatusId
                }
                id
            } else {
                SelectedBooks.insertAndGetId {
                    it[idUser] = userId
                    it[idBook] = bookId
                    it[idStatus] = inProgressStatusId
                }
            }
        }

        // Подсчёт уже прочитанных страниц по этой книге
        val totalAlreadyRead = UsersStatistics
            .innerJoin(SelectedBooks)
            .slice(UsersStatistics.readPages)
            .select { UsersStatistics.idSelectedBook eq selectedBookId }
            .sumOf { it[UsersStatistics.readPages] }

        // Проверка: суммарное количество не должно превышать totalPages
        if (totalAlreadyRead + readPages > totalPages) {
            throw IllegalArgumentException("Суммарное число прочитанных страниц превышает количество страниц в книге.")
            return@transaction
        }

        // Добавляем запись в статистику
        UsersStatistics.insert {
            it[date] = java.time.LocalDateTime.now()
            it[idSelectedBook] = selectedBookId
            it[UsersStatistics.readPages] = readPages
        }
    }*/
    // вспомогательные функции
    fun getStatusIdByName(name: String): EntityID<Int> =
        Statuses.select { Statuses.name eq name }.single()[Statuses.id]

    fun getSelectedBook(userId: EntityID<Int>, bookId: String): ResultRow? =
        SelectedBooks.select { (SelectedBooks.idUser eq userId) and (SelectedBooks.idBook eq bookId) }.singleOrNull()

    fun createSelectedBook(userId: EntityID<Int>, bookId: String, statusId: EntityID<Int>): EntityID<Int> =
        SelectedBooks.insertAndGetId {
            it[idUser] = userId
            it[idBook] = bookId
            it[idStatus] = statusId
        }

    fun updateSelectedBookStatus(selectedBookId: EntityID<Int>, statusId: EntityID<Int>) {
        SelectedBooks.update({ SelectedBooks.id eq selectedBookId }) {
            it[idStatus] = statusId
        }
    }

    fun deleteStatisticsBySelectedBookId(selectedBookId: EntityID<Int>) {
        UsersStatistics.deleteWhere { UsersStatistics.idSelectedBook eq selectedBookId }
    }

    fun addStatistics(selectedBookId: EntityID<Int>, readPages: Int = 0) {
        UsersStatistics.insert {
            it[date] = java.time.LocalDateTime.now()
            it[idSelectedBook] = selectedBookId
            it[UsersStatistics.readPages] = readPages
        }
    }

    //добавление в хочу прочитать
    fun markBookToRead(token: String, bookId: String) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction
        val toReadStatusId = getStatusIdByName("Хочу прочитать")
        val inProgressStatusId = getStatusIdByName("В процессе")
        val readStatusId = getStatusIdByName("Прочитано")

        val existing = getSelectedBook(userId, bookId)

        if (existing == null) {
            createSelectedBook(userId, bookId, toReadStatusId)
            return@transaction
        }

        val selectedBookId = existing[SelectedBooks.id]
        val currentStatusId = existing[SelectedBooks.idStatus]

        when (currentStatusId) {
            toReadStatusId -> throw IllegalStateException("Книга уже добавлена в 'Хочу прочитать'")
            inProgressStatusId -> {
                updateSelectedBookStatus(selectedBookId, toReadStatusId)
                deleteStatisticsBySelectedBookId(selectedBookId)
            }
            readStatusId -> {
                updateSelectedBookStatus(selectedBookId, toReadStatusId)
            }
        }
    }

    //новое добавление в процессе
    fun markBookInProgressNew(token: String, bookId: String, readPages: Int) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction
        val inProgressStatusId = getStatusIdByName("В процессе")

        val book = Books.select { Books.idBook eq bookId }.singleOrNull()
            ?: throw IllegalArgumentException("Книга не найдена")

        val totalPages = book[Books.pages]
        if (readPages > totalPages) {
            throw IllegalArgumentException("Число прочитанных страниц превышает общее количество.")
        }

        val existing = getSelectedBook(userId, bookId)

        val selectedBookId = when {
            existing == null -> createSelectedBook(userId, bookId, inProgressStatusId)

            existing[SelectedBooks.idStatus] != inProgressStatusId -> {
                val id = existing[SelectedBooks.id]
                updateSelectedBookStatus(id, inProgressStatusId)
                id
            }

            else -> existing[SelectedBooks.id]
        }

        val totalRead = UsersStatistics
            .select { UsersStatistics.idSelectedBook eq selectedBookId }
            .sumOf { it[UsersStatistics.readPages] }

        if (totalRead + readPages > totalPages) {
            throw IllegalArgumentException("Суммарное число прочитанных страниц превышает общее количество.")
        }

        addStatistics(selectedBookId, readPages)
    }

    fun markBookAsRead(token: String, bookId: String, rating: Int, comment: String) = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction
        val readStatusId = getStatusIdByName("Прочитано")

        val existing = getSelectedBook(userId, bookId)
        val selectedBookId = if (existing == null) {
            createSelectedBook(userId, bookId, readStatusId)
        } else {
            val id = existing[SelectedBooks.id]
            updateSelectedBookStatus(id, readStatusId)
            id
        }

        deleteStatisticsBySelectedBookId(selectedBookId)
        Reviews.insert {
            it[date] = LocalDate.now()
            it[idSelectedBook] = selectedBookId.value
            it[Reviews.rating] = rating
            it[Reviews.comment] = comment
        }
    }

    // получение записей по книге в процессе
    fun getReadingStats(token: String, bookId: String): List<ReadingStatResponse> = transaction {
        val userId = getUserIdByToken(token) ?: return@transaction emptyList()

        val selectedBook = getSelectedBook(userId, bookId) ?: return@transaction emptyList()

        UsersStatistics
            .select { UsersStatistics.idSelectedBook eq selectedBook[SelectedBooks.id] }
            .orderBy(UsersStatistics.date, SortOrder.DESC)
            .map {
                ReadingStatResponse(
                    date = it[UsersStatistics.date].toString(),
                    readPages = it[UsersStatistics.readPages]
                )
            }
    }
}
