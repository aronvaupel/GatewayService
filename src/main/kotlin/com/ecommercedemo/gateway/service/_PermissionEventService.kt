package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import com.ecommercedemo.gateway.model._permission._Permission
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@EventServiceFor(_Permission::class)
class _PermissionEventService : EventServiceTemplate<_Permission>()