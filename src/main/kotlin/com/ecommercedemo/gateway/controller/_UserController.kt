package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.gateway.model._User
import com.ecommercedemo.gateway.service._UserRestService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Validated
@ControllerFor(_User::class)
@Suppress("ClassName")
class _UserController(
    private val service: _UserRestService
): DownstreamRestServiceTemplate<_User>()