package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.application.validation.password.PasswordCrypto
import com.ecommercedemo.common.application.validation.userrole.UserRole
import com.ecommercedemo.gateway.config.exception.AuthenticationFailureException
import com.ecommercedemo.gateway.config.security.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    @Value("\${security.jwt.access.expiration}")
    private val accessExpiration: Long,
    @Value("\${security.jwt.refresh.expiration}")
    private val refreshExpiration: Long,
    private val _userService: _UserRestService,
    private val _permissionService: _PermissionRestService,
    private val jwtUtil: JwtUtil,
) {
    fun authenticate(username: String, password: String): Triple<String, String, UUID> {
        val user = _userService.getByUsername(username)
        if (!PasswordCrypto.matches(password, user.password)) {
            val permissions = _permissionService.getMultiple(user.permissions, 0, 1000).map { it.label }.toList()
            val accessToken = jwtUtil.generateToken(
                username, user.id, user.userRole, permissions, accessExpiration
            )
            val refreshToken = jwtUtil.generateToken(
                username, user.id, user.userRole, permissions, refreshExpiration
            )
            return Triple(accessToken, refreshToken, user.id)
        } else {
            throw AuthenticationFailureException()
        }
    }

    fun refreshToken(refreshToken: String): Pair<String, String?> {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw AuthenticationFailureException()
        }
        val claims = jwtUtil.getClaimsFromToken(refreshToken)
        val username = claims.subject
        val userId = UUID.fromString(claims["id"].toString())
        val role = UserRole.valueOf(claims["role"].toString())
        val permissions = (claims["permissions"] as List<*>).map { it.toString() }

        val newAccessToken = jwtUtil.generateToken(username, userId, role, permissions, accessExpiration)
        val newRefreshToken = if (jwtUtil.isExpiringSoon(refreshToken)) {
            jwtUtil.generateToken(username, userId, role, permissions, refreshExpiration)
        } else null
        return Pair(newAccessToken, newRefreshToken)
    }

    fun loginAsGuest(): Triple<String, String, UUID> {
        val guestId = UUID.randomUUID()
        val accessToken = jwtUtil.generateToken("guest", guestId, UserRole.GUEST, emptyList(), accessExpiration)
        val refreshToken = jwtUtil.generateToken("guest", guestId, UserRole.GUEST, emptyList(), refreshExpiration)
        return Triple(accessToken, refreshToken, guestId)
    }

    fun logout(response: HttpServletResponse) {
        response.addCookie(Cookie("refreshToken", "").apply {
            isHttpOnly = true
            maxAge = 0
        })
    }

    fun createRefreshCookie(token: String): Cookie {
        return Cookie("refreshToken", token).apply {
            isHttpOnly = true
            maxAge = refreshExpiration.toInt() / 1000
        }
    }
}