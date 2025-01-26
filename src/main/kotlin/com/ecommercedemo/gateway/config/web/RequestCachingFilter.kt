package com.ecommercedemo.gateway.config.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

class RequestCachingFilter : OncePerRequestFilter() {
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("REQUEST CACHING FILTER triggered")
        if (request.requestURI.startsWith("/auth")) {
            println("Detected request for AuthController: skipping request caching")
            filterChain.doFilter(request, response)
            return
        }
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