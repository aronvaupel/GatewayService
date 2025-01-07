package com.ecommercedemo.gateway.persistence._permission

import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import com.ecommercedemo.gateway.model._permission._Permission
import org.springframework.stereotype.Service

@Service
@PersistenceAdapterFor(_Permission::class)
@Suppress("ClassName")
class _PermissionPersistenceAdapter : EntityPersistenceAdapter<_Permission>()