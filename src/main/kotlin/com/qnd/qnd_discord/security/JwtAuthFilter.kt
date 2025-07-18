package com.qnd.qnd_discord.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


/**
 * JWT Authentication Filter that intercepts HTTP requests to validate JWT tokens.
 * This filter checks for the presence of a JWT token in the Authorization header,
 * validates it, and sets up the SecurityContext if the token is valid.
 */
@Component
class JwtAuthFilter(
    /**
     * Service responsible for JWT operations like validation and token parsing
     */
    private val jwtService: JwtService
): OncePerRequestFilter() {
    /**
     * Processes each HTTP request exactly once to validate JWT tokens and set up authentication.
     *
     * @param request The HTTP request being processed
     * @param response The HTTP response being processed
     * @param filterChain The filter chain for passing the request to the next filter
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            if (jwtService.validateAccessToken(authHeader)) {
                val userId = jwtService.getUserIdFromToken(authHeader)
                val auth = UsernamePasswordAuthenticationToken(userId, null, listOf())
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
    }
}