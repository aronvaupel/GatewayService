package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import com.ecommercedemo.gateway.model._User
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service


@Service
@RestServiceFor(_User::class)
@Suppress("ClassName")
class _UserRestService : DownstreamRestServiceTemplate<_User>()