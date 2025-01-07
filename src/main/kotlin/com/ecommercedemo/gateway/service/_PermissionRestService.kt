package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import com.ecommercedemo.gateway.model._permission._Permission
import org.springframework.stereotype.Service

@Service
@Suppress("ClassName")
@RestServiceFor(_Permission::class)
class _PermissionRestService : DownstreamRestServiceTemplate<_Permission>()