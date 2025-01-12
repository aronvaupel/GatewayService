package com.ecommercedemo.gateway.eureka

import com.ecommercedemo.common.application.springboot.EndpointMetadata
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class EurekaPollingService(private val discoveryClient: DiscoveryClient) {

    private val serviceMetadata = mutableMapOf<String, List<EndpointMetadata>>()

    @Scheduled(fixedRate = 30000)
    fun refreshMetadata() {
        discoveryClient.services.forEach { serviceName ->
            val instances = discoveryClient.getInstances(serviceName)
            instances.forEach { instance ->
                val metadata = instance.metadata["endpoints"]
                if (metadata != null) {
                    val parsedMetadata = parseMetadata(metadata)
                    serviceMetadata[serviceName] = parsedMetadata
                }
            }
        }
    }

    fun getMetadataForService(serviceName: String): List<EndpointMetadata>? {
        return serviceMetadata[serviceName]
    }

    private fun parseMetadata(metadata: String): List<EndpointMetadata> {
        return ObjectMapper().readValue(metadata, object : TypeReference<List<EndpointMetadata>>() {})
    }
}

