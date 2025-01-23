package com.ecommercedemo.gateway.config.web

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {

    @Bean
    fun requestCachingFilter(): RequestCachingFilter {
        val registrationBean = FilterRegistrationBean(RequestCachingFilter())
        registrationBean.order = 1
        return RequestCachingFilter()
    }
}