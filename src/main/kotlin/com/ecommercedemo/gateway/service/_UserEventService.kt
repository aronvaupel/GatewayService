package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import com.ecommercedemo.gateway.model._User
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service


@Suppress("ClassName")
@Service
@EventServiceFor(_User::class)
class _UserEventService : EventServiceTemplate<_User>()