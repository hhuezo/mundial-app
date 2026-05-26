package com.itwg.mundial.ui.marcadores

import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.ui.home.FASE_GRUPOS_ID
import com.itwg.mundial.ui.home.HomeFaceUi

data class MarcadoresFaceUi(
    val id: Long,
    val faseId: Long,
    val faseNombre: String,
    val valor: String,
    val partidos: List<PartidoDto>,
    val groups: List<String> = emptyList(),
) {
    val isGroupPhase: Boolean get() = groups.isNotEmpty()
}

fun MarcadoresFaceUi.toCarouselFace(): HomeFaceUi = HomeFaceUi(
    id = id,
    faseId = faseId,
    faseNombre = faseNombre,
    valor = valor,
    totalGanado = 0.0,
    partidosPendientes = 0,
    matches = emptyList(),
    partidosEnFase = partidos.size,
    groups = groups,
)
