package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.dto.auth.AuthRequest
import com.ecommercedemo.gateway.dto.auth.AuthResponse
import com.ecommercedemo.gateway.service.UserActivityTrackerService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val activityTracker: UserActivityTrackerService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody authRequest: AuthRequest,
        response: HttpServletResponse
    ): AuthResponse {
        val username = authRequest.username
        val accessToken = JwtUtil.generateToken(username, 15 * 60 * 1000)  // 15 minutes
        val refreshToken = JwtUtil.generateToken(username, 7 * 24 * 60 * 60 * 1000)  // 7 days
        response.addCookie(Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            maxAge = 7 * 24 * 60 * 60  // 7 days
        })

        // Track last activity
        val userId = UUID.randomUUID()  // Replace with real user ID lookup if necessary
        activityTracker.updateLastActivity(userId, System.currentTimeMillis())

        return AuthResponse(accessToken)
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue("refreshToken") refreshToken: String,
        response: HttpServletResponse
    ): AuthResponse {
        if (JwtUtil.validateToken(refreshToken)) {
            val username = JwtUtil.getUsernameFromToken(refreshToken)
            val newAccessToken = JwtUtil.generateToken(username, 15 * 60 * 1000)  // 15 minutes

            // Update last activity when token is refreshed
            val userId = UUID.randomUUID()  // Replace with real user ID lookup if necessary
            activityTracker.updateLastActivity(userId, System.currentTimeMillis())

            return AuthResponse(newAccessToken)
        } else {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return AuthResponse("")
        }
    }

    @PostMapping("/loginAsGuest")
    fun loginAsGuest(response: HttpServletResponse): AuthResponse {
        val guestUserId = UUID.randomUUID()  // Generate guest user ID (or save to DB)
        val accessToken = JwtUtil.generateToken("guest", 15 * 60 * 1000)  // 15 minutes
        val refreshToken = JwtUtil.generateToken("guest", 7 * 24 * 60 * 60 * 1000)  // 7 days

        response.addCookie(Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            maxAge = 7 * 24 * 60 * 60  // 7 days
        })

        // Track last activity for guest user
        activityTracker.updateLastActivity(guestUserId, System.currentTimeMillis())

        return AuthResponse(accessToken)
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse) {
        // Invalidate the refresh token by removing the cookie
        response.addCookie(Cookie("refreshToken", "").apply {
            isHttpOnly = true
            maxAge = 0
        })
    }
}