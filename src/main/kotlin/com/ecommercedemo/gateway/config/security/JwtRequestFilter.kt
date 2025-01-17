package com.ecommercedemo.gateway.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter

@Service
class JwtRequestFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token != null && jwtUtil.validateToken(token)) {
            val username = jwtUtil.getUsernameFromToken(token)
            val claims = jwtUtil.getClaimsFromToken(token)
            val role = claims["role"].toString()
            val permissions = (claims["permissions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val authorities = mutableSetOf(SimpleGrantedAuthority(role)).apply {
                addAll(permissions.map { SimpleGrantedAuthority(it) })
            }

            val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
            auth.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = auth
        }
        chain.doFilter(request, response)
    }
}