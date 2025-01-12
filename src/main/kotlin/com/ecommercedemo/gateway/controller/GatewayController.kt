package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.eureka.EurekaPollingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.HttpURLConnection
import java.net.URL

@RestController
class GatewayController(
    private val discoveryClient: DiscoveryClient,
    private val pollingService: EurekaPollingService
) {

    @RequestMapping("/{serviceName}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !JwtUtil.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized")
            return
        }

        val role = JwtUtil.getRoleFromToken(token)
        val path = request.requestURI

        val metadata = pollingService.getMetadataForService(serviceName)
        val endpoint = metadata?.firstOrNull { it.path == path }
        if (endpoint == null || !endpoint.roles.contains(role)) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.writer.write("Forbidden")
            return
        }

        val instances = discoveryClient.getInstances(serviceName)
        if (instances.isEmpty()) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write("Service not found: $serviceName")
            return
        }

        val targetUri = instances.first().uri.toString()
        val forwardPath = request.requestURI.replace("/$serviceName", "")
        val fullUrl = "$targetUri$forwardPath"

        try {
            val connection = URL(fullUrl).openConnection() as HttpURLConnection
            connection.requestMethod = request.method
            connection.doOutput = true
            request.headerNames.toList().forEach { header ->
                connection.setRequestProperty(header, request.getHeader(header))
            }
            if (request.method in listOf("POST", "PUT", "PATCH")) {
                connection.outputStream.use { request.inputStream.copyTo(it) }
            }
            response.status = connection.responseCode
            connection.inputStream.use { it.copyTo(response.outputStream) }
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write("Error proxying request: ${e.message}")
        }
    }
}

