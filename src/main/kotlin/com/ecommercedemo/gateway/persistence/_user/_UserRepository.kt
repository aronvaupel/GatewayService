package com.ecommercedemo.gateway.persistence._user

import com.ecommercedemo.common.application.validation.userrole.UserRole
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.gateway.model._user._User
import java.util.*


@Suppress("ClassName", "unused")
interface _UserRepository : EntityRepository<_User, UUID> {
    fun findByUsernameAndPassword(username: String, hashedPassword: String): _User?
    fun countByUserRoleIs(userRole: UserRole): Int
}