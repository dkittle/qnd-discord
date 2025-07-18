package com.qnd.qnd_discord.security

import com.qnd.qnd_discord.database.model.RefreshToken
import com.qnd.qnd_discord.database.model.User
import com.qnd.qnd_discord.database.repository.RefreshTokenRepository
import com.qnd.qnd_discord.database.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

import java.util.UUID


/**
 * Service responsible for handling user authentication, registration, and token management.
 * Provides functionality for JWT-based authentication, including access and refresh tokens.
 */
@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    /**
     * Data class representing a pair of JWT tokens used for authentication.
     *
     * @property accessToken The JWT access token for API authentication
     * @property refreshToken The JWT refresh token for obtaining new access tokens
     */
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    /**
     * Registers a new user with the given email and password.
     *
     * @param email The user's email address
     * @param password The user's password (will be hashed before storage)
     * @return The newly created User entity
     */
    fun registerUser(email: String, password: String): User {
        return userRepository.save(
            User(
                email = email,
                hashedPassword = hashEncoder.encode(password)
            )
        )
    }

    /**
     * Authenticates a user and generates new access and refresh tokens.
     *
     * @param email The user's email address
     * @param password The user's password
     * @return TokenPair containing new access and refresh tokens
     * @throws BadCredentialsException if credentials are invalid
     */
    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email) ?: throw BadCredentialsException("Invalid credentials")

        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())

        storeRefreshToken(user.id!!, newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * Validates a refresh token and generates new access and refresh tokens.
     *
     * @param refreshToken The current refresh token
     * @return TokenPair containing new access and refresh tokens
     * @throws BadCredentialsException if the refresh token is invalid or expired
     */
    @Transactional
    fun refreshAccessToken(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw BadCredentialsException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)!!
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { BadCredentialsException("Invalid refresh token") }
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(UUID.fromString(userId), hashed)
            ?: throw BadCredentialsException("Invalid refresh token")

        refreshTokenRepository.deleteByUserIdAndHashedToken(UUID.fromString(userId), hashed)
        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())
        storeRefreshToken(user.id!!, newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * Stores a hashed refresh token in the database.
     *
     * @param userId The ID of the user the token belongs to
     * @param rawRefreshToken The raw refresh token to be hashed and stored
     */
    fun storeRefreshToken(userId: UUID, rawRefreshToken: String){
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityPeriod
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    /**
     * Hashes a token using SHA-256 algorithm and encodes it in Base64.
     *
     * @param token The token to hash
     * @return Base64 encoded SHA-256 hash of the token
     */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }
}