package com.qnd.qnd_discord.database.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import java.util.*

@Entity(name = "users")
data class User(
    @Email
    val email: String,
    val hashedPassword: String,
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
)

