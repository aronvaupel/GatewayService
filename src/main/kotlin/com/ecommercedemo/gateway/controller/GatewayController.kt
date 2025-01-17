package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.application.springboot.EndpointMetadata
import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.eureka.EurekaPollingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.HttpURLConnection
import java.net.URI
import java.util.*

@RestController
class GatewayController(
    private val discoveryClient: DiscoveryClient,
    private val pollingService: EurekaPollingService,
    private val jwtUtil: JwtUtil
) {
    val log = KotlinLogging.logger {}

    @RequestMapping("/{serviceName}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized")
            return
        }

        val role = jwtUtil.getRoleFromToken(token)
        val path = request.requestURI.removePrefix("/$serviceName")
        val queryParams = request.parameterMap.mapValues { it.value.toList() }

        val metadata = pollingService.getMetadataForService(serviceName)
        val endpoint = metadata?.firstOrNull { matchPathAndValidate(it, path, queryParams) }
        if (endpoint == null || (!endpoint.roles.contains(role) && endpoint.roles.isNotEmpty())) {
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
        val forwardPath = "$targetUri$path"

        try {
            proxyRequest(request, response, forwardPath)
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write("Error proxying request: ${e.message}")
        }
    }

    private fun matchPathAndValidate(
        endpoint: EndpointMetadata,
        path: String,
        queryParams: Map<String, List<String?>>
    ): Boolean {
        val pathRegex = endpoint.path
            .replace("{", "\\{").replace("}", "\\}")
            .replace("\\{[^/]+\\}", "[^/]+").toRegex()

        if (!path.matches(pathRegex)) return false

        val extractedVariables = extractPathVariablesFromPath(endpoint.path, path)
        val validPathVariables = extractedVariables.all { (name, value) ->
            val expectedType = endpoint.pathVariables.firstOrNull { it.name == name }?.typeSimpleName
            validateType(value, expectedType)
        }

        val validQueryParams = queryParams.all { (name, values) ->
            val expectedType = endpoint.requestParameters.firstOrNull { it.name == name }?.typeSimpleName
            values.all { value ->
                value == null || validateType(value, expectedType)
            }
        }

        return validPathVariables && validQueryParams
    }

    private fun extractPathVariablesFromPath(template: String, path: String): Map<String, String> {
        val templateParts = template.split("/")
        val pathParts = path.split("/")

        if (templateParts.size != pathParts.size) return emptyMap()

        return templateParts.zip(pathParts)
            .filter { (templatePart, _) -> templatePart.startsWith("{") && templatePart.endsWith("}") }
            .associate { (templatePart, pathPart) ->
                templatePart.trim('{', '}') to pathPart
            }
    }

    private fun validateType(value: String, expectedType: String?): Boolean {
        return try {
            when (expectedType) {
                "UUID" -> UUID.fromString(value)
                "Int" -> value.toInt()
                "Double" -> value.toDouble()
                "Long" -> value.toLong()
                "Boolean" -> value.toBooleanStrict()
            }
            true
        } catch (e: Exception) {
            log.error { "Failed to validate type: ${e.message}" }
            false
        }
    }

    private fun proxyRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        targetUrl: String
    ) {
        val connection = URI(targetUrl).toURL().openConnection() as HttpURLConnection
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
    }
}
