package com.qnd.qnd_discord.controller

import com.qnd.qnd_discord.database.model.Guild
import com.qnd.qnd_discord.database.repository.GuildRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/guilds")
class GuildController(
    private val repository: GuildRepository
) {

    data class GuildRequest(
        val id: UUID?,
        val serverId: String,
        val name: String,
    )

    data class GuildResponse(
        val id: String,
        val serverId: String,
        val name: String,
        val createdAt: Instant,
    )

    @PostMapping
    fun save(
        @RequestBody body: GuildRequest
    ): GuildResponse {
        val guild = Guild(
            id = body.id,
            serverId = body.serverId,
            name = body.name,
        )
        repository.save(guild)
        return guild.toResponse()
    }

    @GetMapping
    fun findAll(): List<GuildResponse> {
        return repository.findAll().map { it.toResponse() }
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: UUID) {
        repository.deleteById(id)
    }
}