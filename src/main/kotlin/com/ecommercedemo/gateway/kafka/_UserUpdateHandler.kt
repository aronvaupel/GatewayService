package com.ecommercedemo.gateway.kafka

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.handling.abstraction.IUpdateHandler
import com.ecommercedemo.gateway.model._user._User
import com.ecommercedemo.gateway.service._UserEventService
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
class _UserUpdateHandler(
    private val _userEventService: _UserEventService
) : IUpdateHandler<_User> {
    override fun applyChanges(event: EntityEvent) {
        _userEventService.updateByEvent(event)
    }
}