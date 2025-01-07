package com.ecommercedemo.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(
    basePackages = ["com.ecommercedemo.gateway", "com.ecommercedemo.common"]
)
@EntityScan("com.ecommercedemo.gateway")
@EnableJpaRepositories(basePackages = ["com.ecommercedemo.gateway.persistence"])
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
