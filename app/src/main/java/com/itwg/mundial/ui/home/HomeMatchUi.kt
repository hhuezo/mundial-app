package com.itwg.mundial.ui.home

data class HomeMatchUi(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeFlagUrl: String?,
    val awayFlagUrl: String?,
    val dateTime: String,
    val isFinished: Boolean,
    val finalHomeScore: Int?,
    val finalAwayScore: Int?,
    val predictionHomeScore: Int?,
    val predictionAwayScore: Int?,
    val ganado: Double,
)
