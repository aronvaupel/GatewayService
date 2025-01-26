package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.service._UserRestService
import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.util.ContentCachingRequestWrapper
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SuperAdminCreationInterceptorTest {

    private lateinit var userRestService: _UserRestService
    private lateinit var jwtUtil: JwtUtil
    private lateinit var interceptor: SuperAdminCreationInterceptor

    @BeforeEach
    fun setUp() {
        userRestService = mock(_UserRestService::class.java)
        jwtUtil = mock(JwtUtil::class.java)
        interceptor = SuperAdminCreationInterceptor(userRestService, jwtUtil)
    }

    @Test
    fun `should allow creating SUPER_ADMIN when no super admin exists`() {
        val requestBody = """
        {
            "entityClassName": "User",
            "properties": {
                "username": "firstSuperAdmin",
                "userRole": "SUPER_ADMIN",
                "password": "SuperSecurePassword123!"
            }
        }
    """
        val token = "valid.jwt.token"

        val mockRequest = MockHttpServletRequest().apply {
            setContent(requestBody.toByteArray())
            addHeader("Authorization", "Bearer $token")
            contentType = "application/json"
            method = "POST"
        }
        val request = ContentCachingRequestWrapper(mockRequest).apply {
            inputStream.bufferedReader().use { it.readText() }
        }
        val response = MockHttpServletResponse()

        val claims = mock(Claims::class.java).apply {
            `when`(this["role"]).thenReturn("SUPER_ADMIN")
        }

        `when`(userRestService.getSuperAdminCount()).thenReturn(0)
        `when`(jwtUtil.getClaimsFromToken(token)).thenReturn(claims)

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result, "Request should be allowed when no super admin exists")
    }


    @Test
    fun `should reject creating SUPER_ADMIN when super admin count exceeds limit`() {
        val requestBody = """
        {
            "entityClassName": "User",
            "properties": {
                "username": "anotherSuperAdmin",
                "userRole": "SUPER_ADMIN",
                "password": "SecurePassword123!"
            }
        }
    """
        val token = "valid.jwt.token"

        val mockRequest = MockHttpServletRequest().apply {
            setContent(requestBody.toByteArray())
            addHeader("Authorization", "Bearer $token")
            contentType = "application/json"
            method = "POST"
        }
        val request = ContentCachingRequestWrapper(mockRequest).apply {
            inputStream.bufferedReader().use { it.readText() } // Forces content caching
        }
        val response = MockHttpServletResponse()

        val claims = mock(Claims::class.java).apply {
            `when`(this["role"]).thenReturn("ADMIN")
        }

        `when`(userRestService.getSuperAdminCount()).thenReturn(3)
        `when`(jwtUtil.getClaimsFromToken(token)).thenReturn(claims)

        val result = interceptor.preHandle(request, response, Any())

        assertFalse(result, "Request should be rejected when super admin count exceeds the limit")
    }


    @Test
    fun `should reject non-SUPER_ADMIN creating another SUPER_ADMIN`() {
        val request = mock(ContentCachingRequestWrapper::class.java)
        val response = mock(HttpServletResponse::class.java)
        val token = "valid.token"
        val claims = mock(Claims::class.java)

        `when`(request.contentAsByteArray).thenReturn(
            """{
                "entityClassName":"User",
                "properties":{"userRole":"SUPER_ADMIN"}
            }""".toByteArray()
        )
        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(userRestService.getSuperAdminCount()).thenReturn(2)
        `when`(jwtUtil.getClaimsFromToken(token)).thenReturn(claims)

        `when`(claims["role"]).thenReturn("ADMIN")

        val result = interceptor.preHandle(request, response, Any())

        assertFalse(result, "Request should be rejected when non-SUPER_ADMIN tries to create another SUPER_ADMIN")
    }

    @Test
    fun `should allow SUPER_ADMIN to create another SUPER_ADMIN`() {
        val request = mock(ContentCachingRequestWrapper::class.java)
        val response = mock(HttpServletResponse::class.java)
        val token = "valid.token"
        val claims = mock(Claims::class.java)

        `when`(request.contentAsByteArray).thenReturn(
            """{
                "entityClassName":"User",
                "properties":{"userRole":"SUPER_ADMIN"}
            }""".toByteArray()
        )
        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(userRestService.getSuperAdminCount()).thenReturn(1)
        `when`(jwtUtil.getClaimsFromToken(token)).thenReturn(claims)

        `when`(claims["role"]).thenReturn("SUPER_ADMIN")

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result, "Request should be allowed when SUPER_ADMIN creates another SUPER_ADMIN")
    }

    @Test
    fun `should allow creating non-SUPER_ADMIN users`() {
        val request = mock(ContentCachingRequestWrapper::class.java)
        val response = mock(HttpServletResponse::class.java)
        `when`(request.contentAsByteArray).thenReturn(
            """{
                "entityClassName":"User",
                "properties":{"userRole":"USER"}
            }""".toByteArray()
        )

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result, "Request should be allowed for non-SUPER_ADMIN users")
    }
}

