package com.ecommercedemo.gateway.persistence._user

import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import com.ecommercedemo.gateway.model._user._User
import org.springframework.stereotype.Service

@Service
@PersistenceAdapterFor(_User::class)
@Suppress("ClassName")
class _UserPersistenceAdapter : EntityPersistenceAdapter<_User>()