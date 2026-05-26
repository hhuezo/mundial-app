package com.itwg.mundial.data.model

import com.squareup.moshi.Json

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    @Json(name = "token_type") val tokenType: String,
    val user: UserDto,
)

data class MeResponse(
    val user: UserDto,
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    @Json(name = "unidad_id") val unidadId: Long?,
    val unidad: UnidadDto?,
    val activo: Boolean,
    val roles: List<String>,
)

data class UnidadDto(
    val id: Long,
    val nombre: String,
)

data class ResetPasswordRequest(
    @Json(name = "user_id") val userId: Long,
    val password: String,
)

data class ResetPasswordResponse(
    val message: String,
    val user: UserDto,
)

data class MessageResponse(
    val message: String,
)

data class ValidationErrorResponse(
    val message: String?,
    val errors: Map<String, List<String>>?,
)
