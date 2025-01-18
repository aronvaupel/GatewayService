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

@Suppress("HttpUrlsUsage")
@RestController
class GatewayController(
    private val discoveryClient: DiscoveryClient,
    private val pollingService: EurekaPollingService,
    private val jwtUtil: JwtUtil,
) {
    val log = KotlinLogging.logger {}

    @RequestMapping("/{serviceName}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        println("ATTEMPTING TO ROUTE REQUEST TO: $serviceName")
        val token = request.getHeader("Authorization")?.substring(7)
        println("TOKEN RETRIEVED: $token")
        if (token == null || !jwtUtil.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized")
            return
        }

        val role = jwtUtil.getRoleFromToken(token)
        println("ROLE RETRIEVED: $role")
        val path = request.requestURI
        val method = request.method
        println("PATH RETRIEVED: $path")
        val queryParams = request.parameterMap.mapValues { it.value.toList() }
        println("QUERY PARAMS RETRIEVED: $queryParams")
        val preFilteredMetadata = pollingService.getMetadataForService(serviceName)?.filter {
            it.method == method && it.path.contains(serviceName)
        }
        println("PRE-FILTERED METADATA RETRIEVED: $preFilteredMetadata")
        val endpoint = preFilteredMetadata?.firstOrNull { matchPathAndValidate(it, path, queryParams) }
        println("ENDPOINT RETRIEVED: $endpoint")
        if (endpoint == null || (!endpoint.roles.contains(role) && endpoint.roles.isNotEmpty())) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.writer.write("Forbidden")
            return
        }

        val instances = discoveryClient.getInstances(serviceName)
        println("INSTANCES RETRIEVED: $instances")
        if (instances.isEmpty()) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write("Service not found: $serviceName")
            return
        }

        val targetUri = "http://${serviceName}:${instances.first().port}"
        println("TARGET URI RETRIEVED: $targetUri")
        val forwardPath = "$targetUri${path.removePrefix("/$serviceName")}"
        println("FORWARD PATH RETRIEVED: $forwardPath")
        try {
            proxyRequest(request, response, forwardPath)
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write("Error proxying request: ${e.message}")
        }
    }

    private fun matchPathAndValidate(
        candidate: EndpointMetadata,
        path: String,
        queryParams: Map<String, List<String?>>
    ): Boolean {
        val requestSegments = path.split("/").filter { it.isNotEmpty() }
        println("CHECK - REQUEST SEGMENTS: $requestSegments")
        val candidateSegments = candidate.path.split("/").filter { it.isNotEmpty() }
        println("CHECK - CANDIDATE SEGMENTS: $candidateSegments")
        if (requestSegments.size != candidateSegments.size) return false

        candidateSegments.forEachIndexed { index, segment ->
            val requestSegment = requestSegments[index]
            println("CHECK - FIRST LOOP - SEGMENT: $segment,REQUEST SEGMENT: $requestSegment")
            if (!segment.startsWith("{") || !segment.endsWith("}")) {
                if (segment != requestSegment) return false
            } else {
                val variableName = segment.trim('{', '}')
                println("CHECK - FIRST LOOP - VARIABLE NAME: $variableName")
                val variableMetadata = candidate.pathVariables.firstOrNull { it.name == variableName }
                    ?: return false
                if (!validateType(requestSegment, variableMetadata.typeSimpleName)) {
                    return false
                }
            }
        }

        return queryParams.all { (paramName, paramValues) ->
            val paramMetadata = candidate.requestParameters.firstOrNull { it.name == paramName } ?: return false
            paramValues.all { value -> value != null && validateType(value, paramMetadata.typeSimpleName) }
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
        println("CONNECTION OPENED: $connection")
        connection.requestMethod = request.method
        connection.doOutput = true
        request.headerNames.toList().forEach { header ->
            connection.setRequestProperty(header, request.getHeader(header))
        }
        if (request.method in listOf("POST", "PUT", "PATCH")) {
            println("REQUEST METHOD: ${request.method}")
            connection.outputStream.use { request.inputStream.copyTo(it) }
        }
        response.status = connection.responseCode
        connection.inputStream.use { it.copyTo(response.outputStream) }
    }
}
