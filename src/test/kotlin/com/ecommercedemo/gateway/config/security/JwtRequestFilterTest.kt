package com.ecommercedemo.gateway.config.security

import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtRequestFilterTest {

    private lateinit var jwtUtil: JwtUtil
    private lateinit var jwtRequestFilter: JwtRequestFilter

    @BeforeEach
    fun setUp() {
        jwtUtil = mock(JwtUtil::class.java)
        jwtRequestFilter = JwtRequestFilter(jwtUtil)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should skip JWT validation for Swagger paths`() {
        // Arrange
        val request = MockHttpServletRequest()
        request.requestURI = "/swagger-ui"
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // Act
        jwtRequestFilter.doFilterInternal(request, response, chain)

        // Assert
        verify(chain).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should skip JWT validation for API docs paths`() {
        // Arrange
        val request = MockHttpServletRequest()
        request.requestURI = "/v3/api-docs"
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // Act
        jwtRequestFilter.doFilterInternal(request, response, chain)

        // Assert
        verify(chain).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should validate token and set authentication for valid JWT`() {
        val validToken = "valid.token.value"
        val username = "testUser"
        val role = "ROLE_USER"
        val permissions = listOf("PERMISSION_READ", "PERMISSION_WRITE")

        val claims = mock(Claims::class.java)
        `when`(claims["role"]).thenReturn(role)
        `when`(claims["permissions"]).thenReturn(permissions)

        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $validToken")
        }
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        `when`(jwtUtil.validateToken(validToken)).thenReturn(true)
        `when`(jwtUtil.getUsernameFromToken(validToken)).thenReturn(username)
        `when`(jwtUtil.getClaimsFromToken(validToken)).thenReturn(claims)

        jwtRequestFilter.doFilterInternal(request, response, chain)

        verify(chain).doFilter(request, response)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication, "Authentication should be set for a valid token")
        assertTrue(authentication.authorities.any { it.authority == role }, "Role should be present in authorities")
        assertTrue(authentication.authorities.any { it.authority == "PERMISSION_READ" }, "Permission READ should be present")
        assertTrue(authentication.authorities.any { it.authority == "PERMISSION_WRITE" }, "Permission WRITE should be present")
        assertNull(authentication.credentials, "Credentials should be null for security")
    }



    @Test
    fun `should not set authentication for invalid JWT`() {
        val invalidToken = "invalid.token.value"

        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $invalidToken")
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        `when`(jwtUtil.validateToken(invalidToken)).thenReturn(false) // Ensure this stubbing is complete

        jwtRequestFilter.doFilterInternal(request, response, chain)

        verify(chain).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should not set authentication if no Authorization header is provided`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        jwtRequestFilter.doFilterInternal(request, response, chain)

        verify(chain).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }
}
