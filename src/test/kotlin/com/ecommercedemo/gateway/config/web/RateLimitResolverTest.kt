package com.ecommercedemo.gateway.config.security

import com.ecommercedemo.common.application.validation.userrole.UserRole
import com.ecommercedemo.gateway.config.web.RateLimitResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimitResolverTest {

    private lateinit var rateLimitResolver: RateLimitResolver

    @BeforeEach
    fun setUp() {
        rateLimitResolver = RateLimitResolver()
    }

    @Test
    fun `should resolve rate limits for REGISTERED_USER`() {
        val role = UserRole.REGISTERED_USER
        val expected = Pair(20, 40)

        val result = rateLimitResolver.resolveRateLimiter(role)

        assertEquals(expected, result, "Rate limits for REGISTERED_USER should be $expected")
    }

    @Test
    fun `should resolve rate limits for CUSTOMER`() {
        val role = UserRole.CUSTOMER
        val expected = Pair(20, 40)

        val result = rateLimitResolver.resolveRateLimiter(role)

        assertEquals(expected, result, "Rate limits for CUSTOMER should be $expected")
    }

    @Test
    fun `should resolve rate limits for GUEST`() {
        val role = UserRole.GUEST
        val expected = Pair(10, 20)

        val result = rateLimitResolver.resolveRateLimiter(role)

        assertEquals(expected, result, "Rate limits for GUEST should be $expected")
    }

    @Test
    fun `should resolve rate limits for ADMIN`() {
        val role = UserRole.ADMIN
        val expected = Pair(1000, 2000)

        val result = rateLimitResolver.resolveRateLimiter(role)

        assertEquals(expected, result, "Rate limits for ADMIN should be $expected")
    }

    @Test
    fun `should resolve rate limits for SUPER_ADMIN`() {
        val role = UserRole.SUPER_ADMIN
        val expected = Pair(1000, 2000)

        val result = rateLimitResolver.resolveRateLimiter(role)

        assertEquals(expected, result, "Rate limits for SUPER_ADMIN should be $expected")
    }
}
