package com.ecommercedemo.gateway.config.github

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "github")
class GithubProperties {
    lateinit var username: String
    lateinit var token: String
}