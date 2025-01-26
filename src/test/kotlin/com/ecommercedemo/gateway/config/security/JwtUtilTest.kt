package com.ecommercedemo.gateway.config.security

import com.ecommercedemo.common.application.validation.userrole.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JwtUtilTest {

    private lateinit var jwtUtil: JwtUtil
    private val jwtSecret = "testsecretkey12345678901234567890123456789012"

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil()
        jwtUtil.apply {
            val jwtSecretField = JwtUtil::class.java.getDeclaredField("jwtSecret")
            jwtSecretField.isAccessible = true
            jwtSecretField.set(this, jwtSecret)
        }
    }

    @Test
    fun `should generate and validate a token`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.REGISTERED_USER
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = 60000L
        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        assertNotNull(token, "Generated token should not be null")
        assertTrue(jwtUtil.validateToken(token), "Generated token should be valid")
    }

    @Test
    fun `should retrieve username from token`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.REGISTERED_USER
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = 60000L

        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        val extractedUsername = jwtUtil.getUsernameFromToken(token)

        assertEquals(username, extractedUsername, "Extracted username should match the original")
    }

    @Test
    fun `should retrieve role from token`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.ADMIN
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = 60000L

        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        val extractedRole = jwtUtil.getRoleFromToken(token)

        assertEquals(role.toString(), extractedRole, "Extracted role should match the original")
    }

    @Test
    fun `should retrieve claims from token`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.REGISTERED_USER
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = 60000L

        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        val claims = jwtUtil.getClaimsFromToken(token)

        assertEquals(username, claims.subject, "Subject should match the username")
        assertEquals(id.toString(), claims["id"], "ID should match the original")
        assertEquals(role.toString(), claims["role"], "Role should match the original")
        assertEquals(permissions, claims["permissions"], "Permissions should match the original")
    }

    @Test
    fun `should detect if token is expiring soon`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.REGISTERED_USER
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = 3000L

        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        assertTrue(jwtUtil.isExpiringSoon(token, 5000L), "Token should be expiring soon")
    }

    @Test
    fun `should invalidate expired token`() {
        val username = "testUser"
        val id = UUID.randomUUID()
        val role = UserRole.REGISTERED_USER
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")
        val expiration = -1000L

        val token = jwtUtil.generateToken(username, id, role, permissions, expiration)

        assertFalse(jwtUtil.validateToken(token), "Expired token should be invalid")
    }
}
