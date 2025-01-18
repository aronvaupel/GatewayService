package com.ecommercedemo.gateway.config.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class AuthenticationFailureException : RuntimeException() {
    override fun toString(): String {
        return "Authorization failed"
    }
}