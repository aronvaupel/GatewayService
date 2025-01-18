package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.application.validation.password.PasswordCrypto
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import com.ecommercedemo.gateway.model._user._User
import com.ecommercedemo.gateway.persistence._user._UserPersistenceAdapter
import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil
import org.springframework.stereotype.Service


@Service
@RestServiceFor(_User::class)
@Suppress("ClassName")
class _UserRestService(
    private val adapter: _UserPersistenceAdapter
) : DownstreamRestServiceTemplate<_User>() {
    fun getByUsername(username: String): _User? {
        return adapter.getByUsernameAndPassword(username, password)
    }
    fun getSuperAdminCount(): Int {
        return adapter.getSuperAdminCount()
    }
}