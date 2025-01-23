package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.application.springboot.EndpointMetadata
import com.ecommercedemo.gateway.config.security.JwtUtil
import com.ecommercedemo.gateway.eureka.EurekaPollingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.stereotype.Service
import org.springframework.web.util.ContentCachingRequestWrapper
import java.net.HttpURLConnection
import java.net.URI
import java.util.*

@Suppress("HttpUrlsUsage")
@Service
class RequestForwardingService(
    private val jwtUtil: JwtUtil,
    private val pollingService: EurekaPollingService,
    private val discoveryClient: DiscoveryClient
) {
    private val log = KotlinLogging.logger {}


    fun forwardRequest(request: HttpServletRequest, response: HttpServletResponse, serviceName: String) {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized")
            return
        }

        val role = jwtUtil.getRoleFromToken(token)
        val path = request.requestURI
        val method = request.method
        val queryParams = request.parameterMap.mapValues { it.value.toList() }
        val preFilteredMetadata = pollingService.getMetadataForService(serviceName)?.filter {
            it.method == method && it.path.contains(serviceName)
        }
        val endpoint = preFilteredMetadata?.firstOrNull { matchPathAndValidate(it, path, queryParams) }
        if (endpoint == null || (!endpoint.roles.contains(role) && endpoint.roles.isNotEmpty())) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.writer.write("Forbidden...")
            return
        }

        val instances = discoveryClient.getInstances(serviceName)
        if (instances.isEmpty()) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write("Service not found: $serviceName")
            return
        }

        val targetUri = "http://${serviceName}:${instances.first().port}"
        val forwardPath = "$targetUri${path.removePrefix("/$serviceName")}"
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
        val candidateSegments = candidate.path.split("/").filter { it.isNotEmpty() }
        if (requestSegments.size != candidateSegments.size) return false

        candidateSegments.forEachIndexed { index, segment ->
            val requestSegment = requestSegments[index]
            if (!segment.startsWith("{") || !segment.endsWith("}")) {
                if (segment != requestSegment) return false
            } else {
                val variableName = segment.trim('{', '}')
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
        val wrappedRequest = request as? ContentCachingRequestWrapper
            ?: throw IllegalStateException("Request must be wrapped with ContentCachingRequestWrapper")



        val connection = URI(targetUrl).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = request.method
        connection.doOutput = true
        request.headerNames.toList().forEach { header ->
            connection.setRequestProperty(header, request.getHeader(header))
        }
        if (request.method in listOf("POST", "PUT", "PATCH")) {
            val cachedBody = String(wrappedRequest.contentAsByteArray)
            connection.outputStream.use { it.write(cachedBody.toByteArray()) }
        }
        response.status = connection.responseCode
        connection.inputStream.use { it.copyTo(response.outputStream) }
    }

}
