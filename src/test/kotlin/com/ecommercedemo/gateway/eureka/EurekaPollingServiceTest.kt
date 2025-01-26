package com.ecommercedemo.gateway.eureka

import com.ecommercedemo.common.application.springboot.EndpointMetadata
import com.ecommercedemo.common.application.springboot.EndpointMethodParam
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EurekaPollingServiceTest {

    private lateinit var discoveryClient: DiscoveryClient
    private lateinit var eurekaPollingService: EurekaPollingService


    @BeforeEach
    fun setUp() {
        discoveryClient = mock(DiscoveryClient::class.java)
        eurekaPollingService = EurekaPollingService(discoveryClient)
    }

    @Test
    fun `should refresh metadata for services`() {
        // Arrange
        val serviceName = "test-service"
        val metadataJson = """
            [
                {
                    "path": "/test",
                    "method": "GET",
                    "roles": ["USER"],
                    "pathVariables": [{"name": "id", "position": 0, "typeSimpleName": "String"}],
                    "requestParameters": [{"name": "filter", "position": 1, "typeSimpleName": "String"}]
                }
            ]
        """
        val expectedMetadata = listOf(
            EndpointMetadata(
                path = "/test",
                method = "GET",
                roles = setOf("USER"),
                pathVariables = listOf(EndpointMethodParam("id", 0, "String")),
                requestParameters = listOf(EndpointMethodParam("filter", 1, "String"))
            )
        )

        val serviceInstance = mock(ServiceInstance::class.java)
        `when`(serviceInstance.metadata).thenReturn(mapOf("endpoints" to metadataJson))
        `when`(discoveryClient.services).thenReturn(listOf(serviceName))
        `when`(discoveryClient.getInstances(serviceName)).thenReturn(listOf(serviceInstance))

        eurekaPollingService.refreshMetadata()
        val actualMetadata = eurekaPollingService.getMetadataForService(serviceName)
        assertEquals(expectedMetadata, actualMetadata, "Metadata should match the parsed metadata")
    }

    @Test
    fun `should return null for service without metadata`() {
        val serviceName = "unknown-service"
        `when`(discoveryClient.services).thenReturn(emptyList())

        val actualMetadata = eurekaPollingService.getMetadataForService(serviceName)

        assertNull(actualMetadata, "Metadata should be null for unknown services")
    }

    @Test
    fun `should handle empty metadata gracefully`() {
        val serviceName = "empty-service"
        val serviceInstance = mock(ServiceInstance::class.java)
        `when`(serviceInstance.metadata).thenReturn(emptyMap())
        `when`(discoveryClient.services).thenReturn(listOf(serviceName))
        `when`(discoveryClient.getInstances(serviceName)).thenReturn(listOf(serviceInstance))

        eurekaPollingService.refreshMetadata()
        val actualMetadata = eurekaPollingService.getMetadataForService(serviceName)

        assertNull(actualMetadata, "Metadata should be null for services with empty metadata")
    }

    @Test
    fun `should parse multiple services metadata`() {
        // Arrange
        val serviceName1 = "service-one"
        val serviceName2 = "service-two"
        val metadataJson1 = """
            [
                {
                    "path": "/one",
                    "method": "POST",
                    "roles": ["ADMIN"],
                    "pathVariables": [],
                    "requestParameters": [{"name": "param", "position": 0, "typeSimpleName": "String"}]
                }
            ]
        """
        val metadataJson2 = """
            [
                {
                    "path": "/two",
                    "method": "DELETE",
                    "roles": ["USER"],
                    "pathVariables": [{"name": "itemId", "position": 0, "typeSimpleName": "Long"}],
                    "requestParameters": []
                }
            ]
        """

        val expectedMetadata1 = listOf(
            EndpointMetadata(
                path = "/one",
                method = "POST",
                roles = setOf("ADMIN"),
                pathVariables = emptyList(),
                requestParameters = listOf(EndpointMethodParam("param", 0, "String"))
            )
        )
        val expectedMetadata2 = listOf(
            EndpointMetadata(
                path = "/two",
                method = "DELETE",
                roles = setOf("USER"),
                pathVariables = listOf(EndpointMethodParam("itemId", 0, "Long")),
                requestParameters = emptyList()
            )
        )

        val serviceInstance1 = mock(ServiceInstance::class.java)
        val serviceInstance2 = mock(ServiceInstance::class.java)

        `when`(serviceInstance1.metadata).thenReturn(mapOf("endpoints" to metadataJson1))
        `when`(serviceInstance2.metadata).thenReturn(mapOf("endpoints" to metadataJson2))
        `when`(discoveryClient.services).thenReturn(listOf(serviceName1, serviceName2))
        `when`(discoveryClient.getInstances(serviceName1)).thenReturn(listOf(serviceInstance1))
        `when`(discoveryClient.getInstances(serviceName2)).thenReturn(listOf(serviceInstance2))

        eurekaPollingService.refreshMetadata()
        val actualMetadata1 = eurekaPollingService.getMetadataForService(serviceName1)
        val actualMetadata2 = eurekaPollingService.getMetadataForService(serviceName2)

        assertEquals(expectedMetadata1, actualMetadata1, "Metadata for service-one should match")
        assertEquals(expectedMetadata2, actualMetadata2, "Metadata for service-two should match")
    }
}

