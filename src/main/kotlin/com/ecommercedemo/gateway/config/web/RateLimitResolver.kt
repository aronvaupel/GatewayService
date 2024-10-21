package com.ecommercedemo.gateway.config.web

import com.ecommercedemo.userservice.validation.userrole.UserRole
import org.springframework.stereotype.Component

@Component
class RateLimitResolver {

    fun resolveRateLimiter(role: UserRole): Pair<Int, Int> {
        return when (role) {
            UserRole.REGISTERED_USER, UserRole.CUSTOMER -> Pair(20, 40)
            UserRole.GUEST -> Pair(10, 20)
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> Pair(1000, 2000)
        }
    }
}