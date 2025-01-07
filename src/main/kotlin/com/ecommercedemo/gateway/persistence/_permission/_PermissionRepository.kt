package com.ecommercedemo.gateway.persistence._permission

import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.gateway.model._permission._Permission
import java.util.UUID

@Suppress("unused", "ClassName")
interface _PermissionRepository : EntityRepository<_Permission, UUID>