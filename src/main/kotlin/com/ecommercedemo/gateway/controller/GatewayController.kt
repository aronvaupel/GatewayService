package com.ecommercedemo.gateway.controller

import com.ecommercedemo.gateway.service.RequestForwardingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GatewayController(
    private val requestForwardingService: RequestForwardingService
) {

    @RequestMapping("/{serviceName}/**")
    fun routeRequest(
        @PathVariable serviceName: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        requestForwardingService.forwardRequest(request, response, serviceName)
    }

}
