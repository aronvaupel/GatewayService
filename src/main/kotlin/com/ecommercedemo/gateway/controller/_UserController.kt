package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.controller.abstraction.DownstreamRestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.gateway.model._user._User
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "_User API", description = "Endpoints for downstream users.")
@RestController
@RequestMapping("/users")
@Validated
@ControllerFor(_User::class)
@Suppress("ClassName", "unused")
class _UserController : DownstreamRestControllerTemplate<_User>()