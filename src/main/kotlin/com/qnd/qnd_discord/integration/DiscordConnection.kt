package com.qnd.qnd_discord.integration

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DiscordConnection(
    @Value("\${discord.bot.token}") private val botToken: String
): CommandLineRunner {

    override fun run(vararg args: String?) {
        runBlocking {
            connect()
        }
    }

    suspend fun connect() {
        logger.info { "Connecting to Discord..." }
        val kord = Kord(botToken)

        kord.on<MessageCreateEvent> { // runs every time a message is created that our bot can read

            // ignore other bots, even ourselves. We only serve humans here!
            if (message.author?.isBot != false) return@on

            // check if our command is being invoked
            if (message.content != "!ping") return@on

            // all clear, give them the pong!
            message.channel.createMessage("pong!")
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