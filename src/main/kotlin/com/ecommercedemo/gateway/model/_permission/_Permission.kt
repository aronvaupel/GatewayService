package com.ecommercedemo.gateway.model._permission

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.Entity

@Entity
@Suppress("ClassName")
open class _Permission(
    var serviceOfOrigin: String = "",
    var label: String = "",
    var description: String = ""
) : BaseEntity()