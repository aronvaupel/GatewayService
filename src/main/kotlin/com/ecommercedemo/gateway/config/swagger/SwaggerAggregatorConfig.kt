package com.ecommercedemo.gateway.config.swagger

import jakarta.annotation.PostConstruct
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@DependsOn("discoveryClient")
class SwaggerAggregatorConfig(
    private val swaggerUiConfigParameters: SwaggerUiConfigParameters,
    private val discoveryClient: org.springframework.cloud.client.discovery.DiscoveryClient
) {

    @PostConstruct
    fun configureSwagger() {
        val services = discoveryClient.services
        services.forEach { serviceName ->
            val instances = discoveryClient.getInstances(serviceName)
            instances.forEach { instance ->
                val openApiUrl = instance.metadata["openapi-docs"]
                if (!openApiUrl.isNullOrBlank()) {
                    swaggerUiConfigParameters.addGroup(serviceName, openApiUrl)
                }
            }
        }
    }
}