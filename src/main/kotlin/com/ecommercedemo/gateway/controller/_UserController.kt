package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.controller.abstraction.DownstreamRestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.gateway.model._User
import com.ecommercedemo.gateway.service._UserRestService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Validated
@ControllerFor(_User::class)
@Suppress("ClassName", "unused")
class _UserController : DownstreamRestControllerTemplate<_User>()