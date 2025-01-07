package com.ecommercedemo.gateway.persistence._user

import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.gateway.model._user._User
import org.hibernate.validator.constraints.UUID

@Suppress("ClassName", "unused")
interface _UserRepository : EntityRepository<_User, UUID>