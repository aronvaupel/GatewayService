package com.ecommercedemo.gateway.kafka

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.application.kafka.handling.abstraction.IDeleteHandler
import com.ecommercedemo.gateway.model._User
import com.ecommercedemo.gateway.service._UserEventService
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
class _UserDeleteHandler(
    private val _userEventService: _UserEventService
) : IDeleteHandler<_User> {
    override fun applyChanges(event: EntityEvent) {
        _userEventService.deleteByEvent(event)
    }
}