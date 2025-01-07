package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import org.springframework.stereotype.Service

@Service
@Suppress("ClassName")
@RestServiceFor(Permission::class)
class _PermissionRestService : DownstreamRestServiceTemplate<Permission>()