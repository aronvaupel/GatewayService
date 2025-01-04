package com.ecommercedemo.gateway.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class SuperAdminCounter(
    private val redisTemplate: StringRedisTemplate
) {

    private val superAdminCountKey = "user:super_admin:count"

    fun getSuperAdminCount(): Int {
        return redisTemplate.opsForValue().get(superAdminCountKey)?.toInt() ?: 0
    }

    fun incrementSuperAdminCount() {
        redisTemplate.opsForValue().increment(superAdminCountKey, 1)
    }

    fun decrementSuperAdminCount() {
        redisTemplate.opsForValue().decrement(superAdminCountKey, 1)
    }
}
