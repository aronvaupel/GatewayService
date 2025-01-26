package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.application.validation.userrole.UserRole
import com.ecommercedemo.gateway.config.exception.AuthenticationFailureException
import com.ecommercedemo.gateway.config.security.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userService: _UserRestService
    private lateinit var permissionService: _PermissionRestService
    private lateinit var jwtUtil: JwtUtil
    private val accessExpiration = 3600000L
    private val refreshExpiration = 7200000L

    @BeforeEach
    fun setUp() {
        userService = mock(_UserRestService::class.java)
        permissionService = mock(_PermissionRestService::class.java)
        jwtUtil = mock(JwtUtil::class.java)
        authService = AuthService(accessExpiration, refreshExpiration, userService, permissionService, jwtUtil)
    }


    @Test
    fun `should throw AuthenticationFailureException for invalid username`() {
        val username = "invalidUser"
        val password = "testPassword"

        `when`(userService.getByUsername(username)).thenReturn(null)

        assertThrows<AuthenticationFailureException> {
            authService.authenticate(username, password)
        }
    }

    @Test
    fun `should refresh token`() {
        val refreshToken = "refreshToken"
        val username = "testUser"
        val userId = UUID.randomUUID()
        val userRole = UserRole.ADMIN
        val permissions = listOf("PERMISSION_MANAGE")

        val claims = mock(io.jsonwebtoken.Claims::class.java)
        `when`(jwtUtil.validateToken(refreshToken)).thenReturn(true)
        `when`(jwtUtil.getClaimsFromToken(refreshToken)).thenReturn(claims)
        `when`(claims.subject).thenReturn(username)
        `when`(claims["id"]).thenReturn(userId.toString())
        `when`(claims["role"]).thenReturn(userRole.toString())
        `when`(claims["permissions"]).thenReturn(permissions)
        `when`(jwtUtil.generateToken(username, userId, userRole, permissions, accessExpiration))
            .thenReturn("newAccessToken")
        `when`(jwtUtil.isExpiringSoon(refreshToken)).thenReturn(true)
        `when`(jwtUtil.generateToken(username, userId, userRole, permissions, refreshExpiration))
            .thenReturn("newRefreshToken")

        val result = authService.refreshToken(refreshToken)

        assertNotNull(result)
        assertEquals("newAccessToken", result.first, "Access token should match")
        assertEquals("newRefreshToken", result.second, "Refresh token should match")
    }

    @Test
    fun `should login as guest`() {
        val result = authService.loginAsGuest()

        assertNotNull(result)
        verify(jwtUtil).generateToken("guest", result.third, UserRole.GUEST, emptyList(), accessExpiration)
        verify(jwtUtil).generateToken("guest", result.third, UserRole.GUEST, emptyList(), refreshExpiration)
    }

    @Test
    fun `should logout user`() {
        val response = mock(HttpServletResponse::class.java)

        authService.logout(response)

        verify(response).addCookie(any(Cookie::class.java))
    }

    @Test
    fun `should create refresh cookie`() {
        val refreshToken = "refreshToken"

        val cookie = authService.createRefreshCookie(refreshToken)

        assertNotNull(cookie)
        assertEquals(refreshToken, cookie.value)
        assertEquals("refreshToken", cookie.name)
        assertEquals(refreshExpiration.toInt() / 1000, cookie.maxAge)
    }
}
