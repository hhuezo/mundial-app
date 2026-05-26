package com.itwg.mundial.ui.home

const val FASE_GRUPOS_ID = 1L

data class HomeFaceUi(
    val id: Long,
    val faseId: Long,
    val faseNombre: String,
    val valor: String,
    val totalGanado: Double,
    val partidosPendientes: Int,
    val matches: List<HomeMatchUi>,
    /** Si se define, sustituye a [matches].size en el carrusel de fases. */
    val partidosEnFase: Int? = null,
    val usuarios: List<HomeUsuarioUi> = emptyList(),
    /** Solo en fase de grupos (fase_id = 1). */
    val groups: List<String> = emptyList(),
) {
    val isGroupPhase: Boolean get() = groups.isNotEmpty()
}
