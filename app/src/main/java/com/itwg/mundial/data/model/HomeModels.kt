package com.itwg.mundial.data.model

import com.squareup.moshi.Json

data class HomeResponse(
    val faces: List<HomeFaceDto>,
    val grupos: List<String>?,
)

data class HomeFaceDto(
    val id: Long,
    @Json(name = "fase_id") val faseId: Long,
    @Json(name = "fase_nombre") val faseNombre: String,
    val valor: String,
    @Json(name = "total_ganado") val totalGanado: Double,
    val partidos: List<PartidoDto>,
)

data class HomeData(
    val faces: List<HomeFaceDto>,
    val grupos: List<String>?,
) {
    val primaryFace: HomeFaceDto? get() = faces.firstOrNull()
    val totalGanadoSum: Double get() = faces.sumOf { it.totalGanado }
    val totalPartidos: Int get() = faces.sumOf { it.partidos.size }
    val partidosPendientes: Int get() = faces.sumOf { face ->
        face.partidos.count { partido ->
            !partido.finalizado &&
                partido.marcadorUsuarioPais1 == null &&
                partido.marcadorUsuarioPais2 == null
        }
    }
}
