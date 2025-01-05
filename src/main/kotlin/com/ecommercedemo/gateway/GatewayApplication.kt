package com.ecommercedemo.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan("com.ecommercedemo.gateway", "com.ecommercedemo.common")
@EntityScan(basePackages = ["com.ecommercedemo.gateway"])
@EnableJpaRepositories(basePackages = ["com.ecommercedemo.gateway.persistence", "com.ecommercedemo.common.persistence"])
class GatewayApplication

fun main(args: Array<String>) {
	runApplication<GatewayApplication>(*args)
}
