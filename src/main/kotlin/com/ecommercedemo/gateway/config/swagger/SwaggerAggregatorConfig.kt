package com.ecommercedemo.gateway.config.swagger

import jakarta.annotation.PostConstruct
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerAggregatorConfig(
    private val swaggerUiConfigParameters: SwaggerUiConfigParameters,
    private val discoveryClient: org.springframework.cloud.client.discovery.DiscoveryClient
) {

//    @PostConstruct
//    fun configureSwagger() {
//        val services = discoveryClient.services
//        println("SWAGGER DISCOVERED SERVICES: $services")
//        services.forEach { serviceName ->
//            val instances = discoveryClient.getInstances(serviceName)
//            println("SWAGGER DISCOVERED INSTANCES: $serviceName, INSTANCES: $instances")
//            instances.forEach { instance ->
//                val openApiUrl = instance.metadata["openapi-docs"]
//                println("SERVICE: $serviceName, OPENAPI URL: $openApiUrl")
//                if (!openApiUrl.isNullOrBlank()) {
//                    swaggerUiConfigParameters.addGroup(serviceName, openApiUrl)
//                }
//            }
//        }
//    }
}