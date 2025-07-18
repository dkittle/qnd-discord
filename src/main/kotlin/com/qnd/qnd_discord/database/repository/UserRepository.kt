package com.qnd.qnd_discord.database.repository

import com.qnd.qnd_discord.database.model.User
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface UserRepository: CrudRepository<User, UUID> {
    fun findByEmail(email: String): User?
}