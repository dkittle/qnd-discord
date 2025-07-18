package com.qnd.qnd_discord.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


/**
 * Security configuration class that defines the security settings for the application.
 * This class configures HTTP security, CSRF protection, and session management.
 */
@Configuration
class SecurityConfig {

    /**
     * Configures and creates the security filter chain for HTTP requests.
     *
     * @param httpSecurity The HttpSecurity object to configure
     * @return A configured SecurityFilterChain
     */
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .build()
    }
}