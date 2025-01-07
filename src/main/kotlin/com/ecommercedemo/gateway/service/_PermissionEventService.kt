package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.model.concretion.permission.Permission
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@EventServiceFor(Permission::class)
class _PermissionEventService : EventServiceTemplate<Permission>()