package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.gateway.service.UserActivityTrackerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class ActivityInterceptor(
    private val activityTracker: UserActivityTrackerService
) : HandlerInterceptor {

    private val scheduler = Executors.newScheduledThreadPool(1)


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        println("ACTIVITY INTERCEPTOR triggered")
        val userId = request.getHeader("userId")
        if (userId != null) {
            activityTracker.updateLastActivity(UUID.fromString(userId), System.currentTimeMillis())
        }
        return true
    }

    private fun scheduleLogout(userId: UUID) {
        scheduler.schedule({
            val lastActivity = activityTracker.getLastActivity(userId)
            if (lastActivity != null && lastActivity.plusMinutes(2).isBefore(LocalDateTime.now())) {
                println("Automatically logging out user with ID: $userId due to inactivity")
            }
        }, 15, TimeUnit.MINUTES)
    }
}