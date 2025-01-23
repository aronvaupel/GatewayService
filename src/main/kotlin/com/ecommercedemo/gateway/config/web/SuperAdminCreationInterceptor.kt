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
        val createRequest = try {
            val requestBody = request.inputStream.bufferedReader().use { it.readText() }
            ObjectMapper().readValue(requestBody, CreateRequest::class.java)
        } catch (e: Exception) {
            log.error { "Error parsing request body: ${e.message}" }
            return false
        }

        if (createRequest?.entityClassName != "User") return true
        else {
            if (createRequest.properties["userRole"] == "SUPER_ADMIN") {
                val authentication = SecurityContextHolder.getContext().authentication
                val creatorRole = extractRoleFromToken(authentication.credentials.toString())
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
        return claims["role"] as? String
          ?: throw AuthenticationFailureException()
    }
}
