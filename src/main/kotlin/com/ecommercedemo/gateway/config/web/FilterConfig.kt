package com.ecommercedemo.gateway.config.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {

    @Bean
    fun requestCachingFilter(): RequestCachingFilter {
        return RequestCachingFilter()
    }
}