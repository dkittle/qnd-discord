package com.qnd.qnd_discord.database.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant
import java.util.*

@Entity(name = "refresh_tokens")
data class RefreshToken(
    val userId: UUID,
    val expiresAt: Instant,
    val hashedToken: String,
    val created: Instant = Instant.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
)

