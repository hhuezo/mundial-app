package com.itwg.mundial.data.model

data class UserSession(
    val userId: Long,
    val token: String,
    val unidadId: Long?,
    val unidadNombre: String?,
)
