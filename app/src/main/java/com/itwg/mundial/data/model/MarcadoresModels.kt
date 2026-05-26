package com.itwg.mundial.data.model

import com.squareup.moshi.Json

data class MarcadoresResponse(
    val faces: List<MarcadoresFaceDto>,
    val grupos: List<String>?,
)

data class MarcadoresFaceDto(
    val id: Long,
    @Json(name = "fase_id") val faseId: Long,
    @Json(name = "fase_nombre") val faseNombre: String,
    val valor: String,
    val partidos: List<PartidoDto>,
)

data class PartidoDto(
    val id: Long,
    val fecha: String,
    val hora: String,
    @Json(name = "pais1_id") val pais1Id: Long? = null,
    @Json(name = "pais2_id") val pais2Id: Long? = null,
    @Json(name = "fase_id") val faseId: Long,
    @Json(name = "marcador_pais1") val marcadorPais1: Int? = null,
    @Json(name = "marcador_pais2") val marcadorPais2: Int? = null,
    @Json(name = "marcadorUsuario_pais1") val marcadorUsuarioPais1: Int? = null,
    @Json(name = "marcadorUsuario_pais2") val marcadorUsuarioPais2: Int? = null,
    @Json(name = "pais_ganador_id") val paisGanadorId: Long? = null,
    val empate: Boolean? = null,
    val valor: String? = null,
    val ganado: Double? = null,
    val finalizado: Boolean,
    val pais1: PaisDto? = null,
    val pais2: PaisDto? = null,
    val fase: FaseDto,
    @Json(name = "pais_ganador") val paisGanador: PaisDto? = null,
)

data class MarcadorDetailResponse(
    val partido: PartidoDto,
    val usuarios: List<PartidoMarcadorUsuarioDto>,
)

data class PartidoMarcadorUsuarioDto(
    val id: Long,
    val name: String,
    val email: String,
    @Json(name = "marcador_pais1") val marcadorPais1: Int? = null,
    @Json(name = "marcador_pais2") val marcadorPais2: Int? = null,
    val ganado: Double = 0.0,
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
    val valor: String? = null,
    val activo: Boolean? = null,
)

data class UpdateMarcadorRequest(
    @Json(name = "userId") val userId: Long,
    @Json(name = "unidad_id") val unidadId: Long,
    @Json(name = "marcador_pais1") val marcadorPais1: Int,
    @Json(name = "marcador_pais2") val marcadorPais2: Int,
)
