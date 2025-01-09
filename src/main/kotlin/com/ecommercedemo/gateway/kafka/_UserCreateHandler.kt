package com.ecommercedemo.gateway.kafka

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.handling.abstraction.ICreateHandler
import com.ecommercedemo.gateway.model._user._User
import com.ecommercedemo.gateway.service._UserEventService
import org.springframework.stereotype.Service

@Suppress("ClassName", "unused")
@Service
class _UserCreateHandler(
    private val _userEventService: _UserEventService
) : ICreateHandler<_User> {
    override fun applyChanges(event: EntityEvent) {
        _userEventService.createByEvent(event)
    }
}