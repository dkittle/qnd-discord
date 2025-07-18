package com.qnd.qnd_discord.database.repository

import com.qnd.qnd_discord.database.model.Guild
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface GuildRepository: CrudRepository<Guild, UUID> {
}