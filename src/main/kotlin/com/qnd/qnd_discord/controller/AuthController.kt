package com.qnd.qnd_discord.controller

import com.qnd.qnd_discord.database.model.User
import com.qnd.qnd_discord.security.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    data class AuthRequest(
        val email: String,
        val password: String
    )

    data class RefreshRequest(
        val refreshToken: String
    )

    @PostMapping("/register")
    fun register(@RequestBody body: AuthRequest): User =
        authService.registerUser(body.email, body.password)

    @PostMapping("/login")
    fun login(@RequestBody body: AuthRequest): AuthService.TokenPair =
        authService.login(body.email, body.password)

    @PostMapping("/refresh")
    fun refresh(@RequestBody body: RefreshRequest): AuthService.TokenPair =
        authService.refreshAccessToken(body.refreshToken)

}