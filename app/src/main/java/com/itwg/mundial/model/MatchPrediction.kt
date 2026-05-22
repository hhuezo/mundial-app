package com.itwg.mundial.model

import androidx.compose.ui.graphics.Color
import com.itwg.mundial.ui.theme.MarkerEntered
import com.itwg.mundial.ui.theme.MarkerFinished
import com.itwg.mundial.ui.theme.MarkerPending

enum class MatchPredictionStatus {
    PENDING,
    ENTERED,
    FINISHED,
}

data class MatchPrediction(
    val id: String,
    val group: String,
    val homeTeam: String,
    val awayTeam: String,
    val dateTime: String,
    val venue: String,
    val isFinished: Boolean = false,
    val finalHomeScore: Int? = null,
    val finalAwayScore: Int? = null,
)

fun resolveMatchStatus(
    match: MatchPrediction,
    homeScore: String,
    awayScore: String,
): MatchPredictionStatus {
    if (match.isFinished) return MatchPredictionStatus.FINISHED
    if (homeScore.isNotBlank() && awayScore.isNotBlank()) return MatchPredictionStatus.ENTERED
    return MatchPredictionStatus.PENDING
}

fun MatchPredictionStatus.stripeColor(): Color = when (this) {
    MatchPredictionStatus.PENDING -> MarkerPending
    MatchPredictionStatus.ENTERED -> MarkerEntered
    MatchPredictionStatus.FINISHED -> MarkerFinished
}
