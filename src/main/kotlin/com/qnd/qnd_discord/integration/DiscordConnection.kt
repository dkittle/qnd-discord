package com.qnd.qnd_discord.integration

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DiscordConnection(
    @Value("\${discord.bot.token}") private val botToken: String
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        runBlocking {
            connect()
        }
    }

    private suspend fun createChannelHistoryCommand(kord: Kord, guildId: Snowflake) {
        kord.createGuildChatInputCommand(
            guildId,
            "qnd-history",
            "Slash command that retrieves channel history"
        ) {
            string(
                name = "channel_id",
                description = "The channel ID to retrieve history for"
            ) {
                required = true
            }
        }
    }

    private suspend fun getHistoricalMessages(messageId: Snowflake, channel: MessageChannelBehavior) {
        val messageFlow = channel.getMessagesBefore(messageId, 50)
        messageFlow.collect {
            logger.info { "Dated ${it.timestamp}, from ${it.author?.username}" }
            logger.info { it.content }
        }
        logger.info { "Finished collecting messages" }
        channel.createMessage("Done")
    }

    suspend fun connect() {
        logger.info { "Connecting to Discord..." }
        val kord = Kord(botToken)

        kord.guilds.collect {
            logger.info { "Guild found: ${it.name} (${it.id})" }
            createChannelHistoryCommand(kord, it.id)
        }

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            val response = interaction.deferPublicResponse()
            val command = interaction.command
            logger.info { "Command ${command.rootName}" }
            when (command.rootName) {
//                "qnd-history" -> {
//                    val channelId = command.strings["channel_id"]!!.toLong()
//                    getHistoricalMessages(rest, channelId)
//                    response.respond { content = "Getting messages" }
//                }
                else -> {
                    response.respond { content = "Unknown command" }
                }
            }
        }

        kord.on<MessageCreateEvent> { // runs every time a message is created that our bot can read

            // ignore other bots, even ourselves. We only serve humans here!
//            if (message.author?.isBot != false) return@on

            when (message.content.lowercase()) {
                "!qnd-history" -> {
                    message.channel.createMessage("Getting historical messages")
                    getHistoricalMessages(message.id, message.channel)
                }

                else -> logger.info {
                    "${message.editedTimestamp}, Content: '${message.content}' ${
                        message.data.embeds[0].fields.value?.get(
                            0
                        )?.value
                    }"
                }

            }

        }

        kord.login {
            // we need to specify this to receive the content of messages
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }

    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}