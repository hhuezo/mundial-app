package com.itwg.mundial.ui.home

data class HomeUsuarioUi(
    val id: Long,
    val name: String,
    val email: String,
    /** Ganancia en la fase (API: total_ganado). */
    val totalGanado: Double,
    val dineroGanado: Double,
)

enum class HomeViewMode {
    MY_MATCHES,
    UNIT_RANKING,
}
