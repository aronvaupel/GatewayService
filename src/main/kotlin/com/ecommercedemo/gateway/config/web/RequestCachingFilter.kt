package com.ecommercedemo.gateway.config.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.util.ContentCachingRequestWrapper

class RequestCachingFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            val wrappedRequest = ContentCachingRequestWrapper(request)
            chain.doFilter(wrappedRequest, response)
        } else {
            chain.doFilter(request, response)
        }
    }
}