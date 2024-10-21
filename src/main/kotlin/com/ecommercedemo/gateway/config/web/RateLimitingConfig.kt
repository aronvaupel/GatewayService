package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.gateway.config.env.EnvironmentConfig
import com.ecommercedemo.userservice.validation.userrole.UserRole
import io.github.cdimascio.dotenv.Dotenv
import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*
import java.util.concurrent.TimeUnit

@Configuration
class RateLimitingConfig @Autowired constructor(
    private val rateLimitResolver: RateLimitResolver,
    envConfig: EnvironmentConfig,
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val dotenv: Dotenv? = if (envConfig.isLocalEnv) Dotenv.load() else null
    private val jwtSecret = dotenv?.get("JWT_SECRET") ?: System.getenv("JWT_SECRET")

    @Bean
    fun rateLimitingInterceptor(): HandlerInterceptor {
        return object : HandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
                val token = request.getHeader("Authorization")?.substringAfter("Bearer ")
                val role = token?.let { extractRoleFromToken(it) } ?: "guest"
                val (replenishRate, burstCapacity) = rateLimitResolver.resolveRateLimiter(
                    UserRole.valueOf(
                        role.uppercase(
                            Locale.getDefault()
                        )
                    )
                )
                val key = "rate_limit:${role}:${request.remoteAddr}"
                val ops: ValueOperations<String, String> = redisTemplate.opsForValue()
                val current = ops.increment(key, 1)
                if (current == 1L) {
                    redisTemplate.expire(key, replenishRate.toLong(), TimeUnit.SECONDS)
                }
                if (current!! > burstCapacity) {
                    response.status = 429
                    return false
                }
                return true
            }
        }
    }

    private fun extractRoleFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body
        return claims["role"] as String
    }
}