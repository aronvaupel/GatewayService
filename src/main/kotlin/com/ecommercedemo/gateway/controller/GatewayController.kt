package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.service.RequestForwardingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Gateway API", description = "Routes requests to downstream services.")
class GatewayController(
    private val requestForwardingService: RequestForwardingService
) {

    @Operation(summary = "Route requests to downstream services.")
    @RequestMapping("/{serviceName:^(?!(swagger|webjar)).*}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (serviceName == "swagger-ui") {
            response.sendRedirect(request.requestURI)
            return
        }
        requestForwardingService.forwardRequest(request, response, serviceName)
    }

}
