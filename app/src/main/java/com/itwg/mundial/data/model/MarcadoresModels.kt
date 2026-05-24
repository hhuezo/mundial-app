package com.itwg.mundial.data.model

import com.squareup.moshi.Json

data class MarcadoresResponse(
    val partidos: List<PartidoDto>,
    val grupos: List<String>?,
)

data class PartidoDto(
    val id: Long,
    val fecha: String,
    val hora: String,
    @Json(name = "pais1_id") val pais1Id: Long,
    @Json(name = "pais2_id") val pais2Id: Long,
    @Json(name = "fase_id") val faseId: Long,
    @Json(name = "marcador_pais1") val marcadorPais1: Int?,
    @Json(name = "marcador_pais2") val marcadorPais2: Int?,
    @Json(name = "marcadorUsuario_pais1") val marcadorUsuarioPais1: Int?,
    @Json(name = "marcadorUsuario_pais2") val marcadorUsuarioPais2: Int?,
    val valor: String? = null,
    val finalizado: Boolean,
    val pais1: PaisDto,
    val pais2: PaisDto,
    val fase: FaseDto,
)

data class PaisDto(
    val id: Long,
    val nombre: String,
    val grupo: String,
    val bandera: String,
)

data class FaseDto(
    val id: Long,
    val nombre: String,
)

data class UpdateMarcadorRequest(
    @Json(name = "userId") val userId: Long,
    @Json(name = "unidad_id") val unidadId: Long,
    @Json(name = "marcador_pais1") val marcadorPais1: Int,
    @Json(name = "marcador_pais2") val marcadorPais2: Int,
)
