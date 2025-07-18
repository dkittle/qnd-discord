package com.qnd.qnd_discord.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


/**
 * Service responsible for JWT (JSON Web Token) operations including token generation, validation, and parsing.
 * This service handles both access and refresh tokens with different validity periods.
 *
 * @property jwtSecret The base64 encoded secret key used for token signing, injected from application properties
 */
@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val accessTokenValidityPeriod = 15.minutes.inWholeMilliseconds
    val refreshTokenValidityPeriod = 30.days.inWholeMilliseconds

    /**
     * Internal method to generate JWT tokens with specified parameters.
     *
     * @param userId The unique identifier of the user
     * @param type The token type ("access" or "refresh")
     * @param expiry The token validity period in milliseconds
     * @return The generated JWT token string
     */
    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS512)
            .compact()
    }

    /**
     * Generates an access token for the specified user.
     * Access tokens have a shorter validity period and are used for API authentication.
     *
     * @param userId The unique identifier of the user
     * @return The generated access token
     */
    fun generateAccessToken(userId: String): String {
        return generateToken(userId, "access", accessTokenValidityPeriod)
    }

    /**
     * Generates a refresh token for the specified user.
     * Refresh tokens have a longer validity period and are used to obtain new access tokens.
     *
     * @param userId The unique identifier of the user
     * @return The generated refresh token
     */
    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, "refresh", refreshTokenValidityPeriod)
    }

    /**
     * Validates an access token.
     * Checks if the token is of type "access" and hasn't expired.
     *
     * @param token The JWT token to validate (can include "Bearer " prefix)
     * @return true if the token is valid, false otherwise
     */
    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaimsFromToken(token) ?: return false
        val isAccessToken = claims["type"] == "access" as? String ?: return false
        return isAccessToken && claims.expiration.after(Date())
    }

    /**
     * Validates a refresh token.
     * Checks if the token is of type "refresh" and hasn't expired.
     *
     * @param token The JWT token to validate (can include "Bearer " prefix)
     * @return true if the token is valid, false otherwise
     */
    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaimsFromToken(token) ?: return false
        val isRequestToken = claims["type"] == "refresh" as? String ?: return false
        return isRequestToken && claims.expiration.after(Date())
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token The JWT token to parse (can include "Bearer " prefix)
     * @return The user ID if token is valid, null otherwise
     * @throws IllegalStateException if the token is invalid
     */
    fun getUserIdFromToken(token: String): String? {
        val claims = parseAllClaimsFromToken(token) ?: error("Invalid token")
        return claims.subject
    }

    /**
     * Internal method to parse and validate JWT token claims.
     * Handles "Bearer " prefix removal and token signature verification.
     *
     * @param token The JWT token to parse (can include "Bearer " prefix)
     * @return The token claims if valid, null if parsing fails
     */
    private fun parseAllClaimsFromToken(token: String): Claims? {
        return try {
            val rawToken =
                if (token.startsWith("Bearer "))
                    token.removePrefix("Bearer ")
                else token
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}