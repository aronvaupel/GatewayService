package com.ecommercedemo.gateway.service

import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


@Service
@DependsOn("redisTemplate")
class UserActivityTrackerService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun updateLastActivity(userId: UUID, lastActivityTimestamp: Long) {
        val lastActivity = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActivityTimestamp), ZoneId.systemDefault())
        redisTemplate.opsForValue().set("user:lastActivity:$userId", lastActivityTimestamp)
    }

    fun getLastActivity(userId: UUID): LocalDateTime? {
        val lastActivityTimestamp = redisTemplate.opsForValue().get("user:lastActivity:$userId") as Long?
        return lastActivityTimestamp?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }
}