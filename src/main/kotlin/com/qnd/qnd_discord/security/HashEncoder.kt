package com.qnd.qnd_discord.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component


/**
 * A component that provides password hashing and verification functionality using BCrypt encryption.
 * This class wraps Spring Security's BCryptPasswordEncoder to provide a simplified interface
 * for password handling operations.
 */
@Component
class HashEncoder {

    private val bCrypt = BCryptPasswordEncoder()

    /**
     * Encodes a raw password string using BCrypt hashing algorithm.
     *
     * @param raw The plain text password to be encoded
     * @return The BCrypt hashed version of the password
     */
    fun encode(raw: String): String {
        return bCrypt.encode(raw)
    }

    /**
     * Verifies if a raw password matches a hashed password.
     *
     * @param raw The plain text password to check
     * @param hashed The hashed password to compare against
     * @return true if the passwords match, false otherwise
     */
    fun matches(raw: String, hashed: String): Boolean {
        return bCrypt.matches(raw, hashed)
    }
}