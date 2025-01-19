package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.service.RequestForwardingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Gateway API", description = "Routes requests to downstream services.")
class GatewayController(
    private val requestForwardingService: RequestForwardingService
) {
    @Operation(summary = "Serve Swagger UI directly.")
    @GetMapping("/swagger-ui/**")
    fun serveSwaggerUI(request: HttpServletRequest, response: HttpServletResponse) {
        request.getRequestDispatcher(request.requestURI).forward(request, response)
    }

    @Operation(summary = "Route requests to downstream services.")
    @RequestMapping("/{serviceName}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        requestForwardingService.forwardRequest(request, response, serviceName)
    }

}
