package com.ecommercedemo.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(
	basePackages = ["com.ecommercedemo.gateway", "com.ecommercedemo.common"],
	excludeFilters = [ComponentScan.Filter(
		type = FilterType.REGEX,
		pattern = ["com\\.ecommercedemo\\.common\\.model\\.concretion\\._pseudoProperty\\..*"]
	)]
)

@EntityScan("com.ecommercedemo.gateway")
@EnableJpaRepositories(
	basePackages = ["com.ecommercedemo.gateway.persistence"],
	excludeFilters = [ComponentScan.Filter(
		type = FilterType.REGEX,
		pattern = ["com\\.ecommercedemo\\.common\\persistence\\..*"]
	)]
)
class GatewayApplication

fun main(args: Array<String>) {
	runApplication<GatewayApplication>(*args)
}
