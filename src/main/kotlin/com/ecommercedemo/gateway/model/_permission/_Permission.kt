package com.ecommercedemo.gateway.model._permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Entity

@Entity
@Suppress("ClassName")
open class _Permission(
    open var serviceOfOrigin: String = "",
    open var label: String = "",
    open var description: String = ""
) : BaseEntity()