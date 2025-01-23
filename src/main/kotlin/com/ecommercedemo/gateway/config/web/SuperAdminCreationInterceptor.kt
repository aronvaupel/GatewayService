package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.gateway.config.exception.AuthenticationFailureException
import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.service._UserRestService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import mu.KotlinLogging
import org.springframework.web.util.ContentCachingRequestWrapper

@Component
class SuperAdminCreationInterceptor(
    private val _userRestService: _UserRestService,
    private val jwtUtil: JwtUtil
) : HandlerInterceptor {

    val log = KotlinLogging.logger {}

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        println("SUPER ADMIN CREATION INTERCEPTOR triggered")
        val wrappedRequest = request as? ContentCachingRequestWrapper
            ?: throw IllegalStateException("Request must be wrapped with ContentCachingRequestWrapper")
        val createRequest = try {
            val requestBody = String(wrappedRequest.contentAsByteArray)
            ObjectMapper().readValue(requestBody, CreateRequest::class.java)
        } catch (e: Exception) {
            log.error { "Error parsing request body: ${e.message}" }
            return false
        }

        if (createRequest?.entityClassName != "User") return true
        else {
            if (createRequest.properties["userRole"] == "SUPER_ADMIN") {
                val authHeader = request.getHeader("Authorization")
                val token = authHeader.removePrefix("Bearer ").trim()
                val creatorRole = extractRoleFromToken(token)
                val superAdminCount = _userRestService.getSuperAdminCount()
                println("Super admin count: $superAdminCount")
                when {
                    superAdminCount == 0 -> return true
                    superAdminCount >= 3 -> return false
                    superAdminCount in 1..2 && creatorRole != "SUPER_ADMIN" -> return false
                }
            }
        }
        return true
    }

    private fun extractRoleFromToken(token: String): String {
        val claims = jwtUtil.getClaimsFromToken(token)
        val result = claims["role"] as? String
        println("Role extracted from token: $result")
        return result
          ?: throw AuthenticationFailureException()
    }
}
