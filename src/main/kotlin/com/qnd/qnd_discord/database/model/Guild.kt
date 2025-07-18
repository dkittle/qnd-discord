package com.qnd.qnd_discord.database.model

import com.qnd.qnd_discord.controller.GuildController
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant
import java.util.*

@Entity(name = "guilds")
data class Guild(
    val serverId: String,
    val name: String,
    val createdAt: Instant = Instant.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
) {
    fun toResponse(): GuildController.GuildResponse {
        return GuildController.GuildResponse(
            id = id.toString(),
            serverId = serverId,
            name = name,
            createdAt = createdAt
        )
    }
}