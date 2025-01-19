package com.ecommercedemo.gateway.model._user

import com.ecommercedemo.common.application.validation.password.PasswordCrypto
import com.ecommercedemo.common.application.validation.password.PasswordValidator
import com.ecommercedemo.common.application.validation.password.ValidPassword
import com.ecommercedemo.common.application.validation.userrole.UserRole
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

@Schema(description = "Represents a user entity within the gateway.")
@Entity
@Table(name = "_users")
@Suppress("ClassName")
open class _User(
    @Schema(description = "The username of the user.", example = "johndoe")
    @field:NotBlank(message = "Username is mandatory")
    @field:Size(max = 50, message = "Username must be less than 50 characters")
    open var username: String = "",

    @ValidPassword
    @NotBlank(message = "Password is mandatory")
    private var _password: String = "",

    @Enumerated(EnumType.ORDINAL)
    @Schema(description = "The role of the user.", example = "ADMIN")
    open var userRole: UserRole = UserRole.GUEST,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_permissions",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")]
    )
    @Column(name = "permission_id", nullable = false)
    @Schema(description = "List of permissions assigned to the user.")
    open var permissions: List<UUID> = listOf(),
) : AugmentableBaseEntity() {
    open var password: String
        get() = _password
        set(value) {
            if (!PasswordValidator.isValid(value, null)) {
                throw IllegalArgumentException("Password does not meet the requirements")
            }
            _password = PasswordCrypto.hashPassword(value)
        }
}