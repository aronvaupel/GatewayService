package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.userservice.service.user.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

@Component
class ActivityInterceptor(
    private val userService: UserService
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val userId = request.getHeader("userId")
        if (userId != null) {
            userService.updateLastActivity(UUID.fromString(userId), System.currentTimeMillis())
        }
        return true
    }
}