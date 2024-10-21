package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.security.JwtUtil
import com.ecommercedemo.userservice.dto.auth.AuthRequest
import com.ecommercedemo.userservice.dto.auth.AuthResponse
import com.ecommercedemo.userservice.service.user.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody authRequest: AuthRequest,
        response: HttpServletResponse
    ): AuthResponse {
        val username = authRequest.username
        val accessToken = JwtUtil.generateToken(username, 15 * 60 * 1000) // 15 minutes
        val refreshToken = JwtUtil.generateToken(username, 7 * 24 * 60 * 60 * 1000) // 7 days
        response.addCookie(Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            maxAge = 7 * 24 * 60 * 60 // 7 days
        })

        return AuthResponse(accessToken)
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue("refreshToken") refreshToken: String,
        response: HttpServletResponse
    ): AuthResponse {
        if (JwtUtil.validateToken(refreshToken)) {
            val username = JwtUtil.getUsernameFromToken(refreshToken)
            val user = userService.getUserByUsername(username)
            val newAccessToken = JwtUtil.generateToken(username, 15 * 60 * 1000) // 15 minutes
            userService.updateLastActivity(user.id, System.currentTimeMillis())
            return AuthResponse(newAccessToken)
        } else {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return AuthResponse("")
        }
    }

    @PostMapping("/loginAsGuest")
    fun loginAsGuest(response: HttpServletResponse): AuthResponse {
        val savedGuestUser = userService.registerGuest()
        val accessToken = JwtUtil.generateToken(savedGuestUser.username, 15 * 60 * 1000) // 15 minutes
        val refreshToken = JwtUtil.generateToken(savedGuestUser.username, 7 * 24 * 60 * 60 * 1000) // 7 days
        response.addCookie(Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            maxAge = 7 * 24 * 60 * 60 // 7 days
        })
        userService.updateLastActivity(savedGuestUser.id, System.currentTimeMillis())
        userService.scheduleDeletion(savedGuestUser.id, 7 * 24 * 60 * 60 * 1000)
        return AuthResponse(accessToken)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("userId") userId: String,
        response: HttpServletResponse
    ) {
        response.addCookie(Cookie("refreshToken", "").apply {
            isHttpOnly = true
            maxAge = 0
        })
        userService.updateLastActivity(UUID.fromString(userId), System.currentTimeMillis())
    }
}