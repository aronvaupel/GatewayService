package com.ecommercedemo.gateway.config.security

import com.ecommercedemo.common.application.validation.userrole.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil {
    @Value("\${security.jwt.secret}")
    private lateinit var jwtSecret: String

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(
        username: String,
        id: UUID,
        role: UserRole,
        permissions: List<String>,
        expiration: Long): String {
        return Jwts.builder()
            .setSubject(username)
            .claim("id", id)
            .claim("role", role.toString())
            .claim("permissions", permissions)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject
    }

    fun getRoleFromToken(token: String) = getClaimsFromToken(token)["role"] as String


    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun isExpiringSoon(token: String, thresholdMillis: Long = 300000): Boolean { // 5 minutes
        val expiration = getClaimsFromToken(token).expiration
        return expiration.time - System.currentTimeMillis() <= thresholdMillis
    }
}