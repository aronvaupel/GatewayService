package com.ecommercedemo.gateway.service

import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


@Service
class UserActivityTrackerService(
    private val redisTemplate: StringRedisTemplate
) {

    fun updateLastActivity(userId: UUID, lastActivityTimestamp: Long) {
        val lastActivity = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActivityTimestamp), ZoneId.systemDefault())
        redisTemplate.opsForValue().set("user:lastActivity:$userId", lastActivityTimestamp.toString())
    }

    fun getLastActivity(userId: UUID): LocalDateTime? {
        val lastActivityTimestamp = redisTemplate.opsForValue().get("user:lastActivity:$userId")?.toLong()
        return lastActivityTimestamp?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }
}