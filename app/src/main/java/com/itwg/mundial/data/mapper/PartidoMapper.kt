package com.itwg.mundial.data.mapper

import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.model.MatchPrediction
import com.itwg.mundial.ui.home.HomeMatchUi
private val MONTHS_ES = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

fun PartidoDto.toHomeMatchUi(): HomeMatchUi {
    val home = pais1
    val away = pais2
    return HomeMatchUi(
        id = id.toString(),
        homeTeam = home?.nombre ?: "Por definir",
        awayTeam = away?.nombre ?: "Por definir",
        homeFlagUrl = home?.bandera,
        awayFlagUrl = away?.bandera,
        dateTime = formatPartidoDateTime(fecha, hora),
        isFinished = finalizado,
        finalHomeScore = if (finalizado) marcadorPais1 else null,
        finalAwayScore = if (finalizado) marcadorPais2 else null,
        predictionHomeScore = marcadorUsuarioPais1,
        predictionAwayScore = marcadorUsuarioPais2,
        ganado = ganado ?: 0.0,
    )
}

fun PartidoDto.toMatchPrediction(): MatchPrediction {
    val home = pais1
    val away = pais2
    val group = home?.grupo ?: away?.grupo ?: "-"
    return MatchPrediction(
        id = id.toString(),
        group = group,
        homeTeam = home?.nombre ?: "Por definir",
        awayTeam = away?.nombre ?: "Por definir",
        homeFlagUrl = home?.bandera,
        awayFlagUrl = away?.bandera,
        dateTime = formatPartidoDateTime(fecha, hora),
        venue = fase.nombre,
        isFinished = finalizado,
        finalHomeScore = if (finalizado) marcadorPais1 else null,
        finalAwayScore = if (finalizado) marcadorPais2 else null,
        predictionHomeScore = marcadorUsuarioPais1,
        predictionAwayScore = marcadorUsuarioPais2,
    )
}

fun formatPartidoDateTime(fecha: String, hora: String): String {
    return try {
        val datePart = fecha.substring(0, 10)
        val parts = datePart.split("-")
        if (parts.size != 3) return "$datePart • ${hora.take(5)}"
        val day = parts[2].toInt()
        val monthIndex = parts[1].toInt().coerceIn(1, 12) - 1
        val time = hora.take(5)
        "$day ${MONTHS_ES[monthIndex]} • $time"
    } catch (_: Exception) {
        "${fecha.take(10)} • ${hora.take(5)}"
    }
}

fun List<PartidoDto>.filterByGroup(group: String): List<MatchPrediction> =
    filter { it.pais1?.grupo == group }.map { it.toMatchPrediction() }

fun List<PartidoDto>.availableGroups(): List<String> =
    mapNotNull { it.pais1?.grupo }
        .distinct()
        .sorted()
