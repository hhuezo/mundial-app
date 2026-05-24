package com.itwg.mundial.ui.home

data class HomeFaceUi(
    val id: Long,
    val faseId: Long,
    val faseNombre: String,
    val valor: String,
    val totalGanado: Double,
    val partidosPendientes: Int,
    val matches: List<HomeMatchUi>,
)
