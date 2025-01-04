package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.service.SuperAdminCounter
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SuperAdminCreationInterceptor(
    private val superAdminCounter: SuperAdminCounter
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val createRequest = try {
            val requestBody = request.inputStream.bufferedReader().use { it.readText() }
            ObjectMapper().readValue(requestBody, CreateRequest::class.java)
        } catch (e: Exception) {
            return true
        }

        if (createRequest.entityClassName != "User") return true
        else {
            if (createRequest.properties["userRole"] == "SUPER_ADMIN") {
                val authentication = SecurityContextHolder.getContext().authentication
                val creatorRole = extractRoleFromToken(authentication.credentials.toString())
                val superAdminCount = superAdminCounter.getSuperAdminCount()

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
        val claims = JwtUtil.getClaimsFromToken(token)
        return ""
//        return claims["role"] as? String
//            ?: throw AccessDeniedException("Unauthorized: Role claim is missing in token")
    }
}
