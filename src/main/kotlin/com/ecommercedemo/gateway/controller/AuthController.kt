package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.dto.auth.AuthRequest
import com.ecommercedemo.gateway.dto.auth.AuthResponse
import com.ecommercedemo.gateway.service.AuthService
import com.ecommercedemo.gateway.service.UserActivityTrackerService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val activityTracker: UserActivityTrackerService,
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody authRequest: AuthRequest,
        response: HttpServletResponse
    ): AuthResponse {
        val (accessToken, refreshToken, userId) = authService.authenticate(authRequest.username, authRequest.password)
        response.addCookie(authService.createRefreshCookie(refreshToken))
        activityTracker.updateLastActivity(userId, System.currentTimeMillis())
        return AuthResponse(accessToken)
    }

    @PostMapping("/refresh")
    fun refresh(@CookieValue("refreshToken") refreshToken: String, response: HttpServletResponse): AuthResponse {
        val (newAccessToken, updatedRefreshToken) = authService.refreshToken(refreshToken)
        if (updatedRefreshToken != null) {
            response.addCookie(authService.createRefreshCookie(updatedRefreshToken))
        }
        return AuthResponse(newAccessToken)
    }

    @PostMapping("/loginAsGuest")
    fun loginAsGuest(response: HttpServletResponse): AuthResponse {
        val (accessToken, refreshToken, guestId) = authService.loginAsGuest()
        response.addCookie(authService.createRefreshCookie(refreshToken))
        activityTracker.updateLastActivity(guestId, System.currentTimeMillis())
        return AuthResponse(accessToken)
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse) {
        authService.logout(response)
    }

}
