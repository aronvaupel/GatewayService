package com.ecommercedemo.gateway.config.swagger

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class SwaggerAggregatorConfig(
    private val discoveryClient: DiscoveryClient
) {

    @Scheduled(fixedRate = 30000)
    fun configureSwagger() {
        val services = discoveryClient.services
        println("SWAGGER DISCOVERED SERVICES: $services")
        services.forEach { serviceName ->
            val instances = discoveryClient.getInstances(serviceName)
            println("SWAGGER DISCOVERED INSTANCES: $serviceName, INSTANCES: $instances")
            instances.forEach { instance ->
                val openApiUrl = instance.metadata["openapi-docs"]
                println("SERVICE: $serviceName, OPENAPI URL: $openApiUrl")
                if (!openApiUrl.isNullOrBlank()) {
                    GroupedOpenApi.builder()
                        .group(serviceName)
                        .pathsToMatch("/$openApiUrl")
                        .displayName("OpenAPI for $serviceName")
                        .build()
                    println("Registered Swagger Group: $serviceName with URL: $openApiUrl")
                }
            }
        }
    }
}
