package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import com.ecommercedemo.gateway.model._User
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service


@Suppress("ClassName")
@Service
@EventServiceFor(_User::class)
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
class _UserEventService : EventServiceTemplate<_User>()