package com.ecommercedemo.gateway.service

import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserActivityTrackerServiceTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var service: UserActivityTrackerService

    @BeforeEach
    fun setUp() {
        redisTemplate = mock(StringRedisTemplate::class.java)
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        service = UserActivityTrackerService(redisTemplate)
    }

    @Test
    fun `should update last activity`() {
        val userId = UUID.randomUUID()
        val timestamp = System.currentTimeMillis()

        service.updateLastActivity(userId, timestamp)

        verify(valueOperations).set("user:lastActivity:$userId", timestamp.toString())
    }

    @Test
    fun `should return last activity if it exists`() {
        val userId = UUID.randomUUID()
        val timestamp = System.currentTimeMillis()
        val expectedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

        `when`(valueOperations.get("user:lastActivity:$userId")).thenReturn(timestamp.toString())

        val lastActivity = service.getLastActivity(userId)

        assertEquals(expectedDateTime, lastActivity, "Last activity should match the stored value")
    }

    @Test
    fun `should return null if last activity does not exist`() {
        val userId = UUID.randomUUID()

        `when`(valueOperations.get("user:lastActivity:$userId")).thenReturn(null)

        val lastActivity = service.getLastActivity(userId)

        assertNull(lastActivity, "Last activity should be null if no value is stored in Redis")
    }
}
