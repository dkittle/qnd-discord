package com.qnd.qnd_discord.database.repository

import com.qnd.qnd_discord.database.model.RefreshToken
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface RefreshTokenRepository: CrudRepository<RefreshToken, UUID> {
    fun findByUserIdAndHashedToken(userId: UUID, hashedToken: String): RefreshToken?
    fun deleteByUserIdAndHashedToken(userId: UUID, hashedToken: String)
}