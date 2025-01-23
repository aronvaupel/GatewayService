package com.ecommercedemo.gateway.config.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

class RequestCachingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("REQUEST CACHING FILTER triggered")
        val wrappedRequest = if (request is ContentCachingRequestWrapper) {
            request
        } else {
            ContentCachingRequestWrapper(request)
        }
        wrappedRequest.inputStream.bufferedReader().use { it.readText() }
        println("Request body: ${String(wrappedRequest.contentAsByteArray)}")
        filterChain.doFilter(wrappedRequest, response)
    }
}