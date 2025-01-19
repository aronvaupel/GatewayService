package com.ecommercedemo.gateway.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for user authentication.")
data class AuthRequest(
    @Schema(description = "Username of the user.", example = "johndoe")
    val username: String,
    @Schema(description = "Password of the user.", example = "password123")
    val password: String
)
