package com.ecommercedemo.gateway.controller

import com.ecommercedemo.common.controller.abstraction.DownstreamRestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.gateway.model._permission._Permission
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "_Permission API", description = "Endpoints for downstream permissions.")
@RestController
@RequestMapping("/permissions")
@Suppress("ClassName", "unused")
@ControllerFor(_Permission::class)
class _PermissionController : DownstreamRestControllerTemplate<_Permission>()