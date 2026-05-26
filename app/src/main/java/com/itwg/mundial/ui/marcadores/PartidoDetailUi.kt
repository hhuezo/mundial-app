package com.itwg.mundial.ui.marcadores

data class PartidoDetailUi(
    val id: Long,
    val dateTime: String,
    val faseNombre: String,
    val grupoLabel: String?,
    val homeTeam: String,
    val awayTeam: String,
    val homeFlagUrl: String?,
    val awayFlagUrl: String?,
    val isFinished: Boolean,
    val finalHomeScore: Int?,
    val finalAwayScore: Int?,
    val usuarios: List<PartidoUsuarioMarcadorUi>,
)

data class PartidoUsuarioMarcadorUi(
    val id: Long,
    val name: String,
    val email: String,
    val marcadorHome: Int?,
    val marcadorAway: Int?,
    val ganado: Double,
    val hasPrediction: Boolean,
)
