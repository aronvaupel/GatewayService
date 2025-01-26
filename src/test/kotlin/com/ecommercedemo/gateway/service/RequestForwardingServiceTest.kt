package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.application.springboot.EndpointMetadata
import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.eureka.EurekaPollingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.cloud.client.discovery.DiscoveryClient
import java.io.PrintWriter

class RequestForwardingServiceTest {

    private lateinit var jwtUtil: JwtUtil
    private lateinit var pollingService: EurekaPollingService
    private lateinit var discoveryClient: DiscoveryClient
    private lateinit var forwardingService: RequestForwardingService

    @BeforeEach
    fun setUp() {
        jwtUtil = mock(JwtUtil::class.java)
        pollingService = mock(EurekaPollingService::class.java)
        discoveryClient = mock(DiscoveryClient::class.java)
        forwardingService = RequestForwardingService(jwtUtil, pollingService, discoveryClient)
    }

    private fun mockResponse(): HttpServletResponse {
        val response = mock(HttpServletResponse::class.java)
        val writer = mock(PrintWriter::class.java)
        `when`(response.writer).thenReturn(writer)
        return response
    }

    @Test
    fun `should return unauthorized for invalid token`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mockResponse()

        `when`(request.getHeader("Authorization")).thenReturn(null)

        forwardingService.forwardRequest(request, response, "testService")

        verify(response).status = HttpServletResponse.SC_UNAUTHORIZED
        verify(response.writer).write("Unauthorized")
    }

    @Test
    fun `should return forbidden for missing or invalid metadata`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mockResponse()
        val token = "validToken"

        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(jwtUtil.validateToken(token)).thenReturn(true)
        `when`(pollingService.getMetadataForService("testService")).thenReturn(null)

        forwardingService.forwardRequest(request, response, "testService")

        verify(response).status = HttpServletResponse.SC_FORBIDDEN
        verify(response.writer).write("Forbidden...")
    }

    @Test
    fun `should return forbidden for insufficient role`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mockResponse()
        val token = "validToken"

        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(jwtUtil.validateToken(token)).thenReturn(true)
        `when`(jwtUtil.getRoleFromToken(token)).thenReturn("GUEST")
        `when`(pollingService.getMetadataForService("testService")).thenReturn(
            listOf(
                EndpointMetadata(
                    path = "/resource",
                    method = "GET",
                    roles = setOf("USER"),
                    pathVariables = emptyList(),
                    requestParameters = emptyList()
                )
            )
        )

        forwardingService.forwardRequest(request, response, "testService")

        verify(response).status = HttpServletResponse.SC_FORBIDDEN
        verify(response.writer).write("Forbidden...")
    }
}
