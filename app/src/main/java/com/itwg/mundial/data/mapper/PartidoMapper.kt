package com.itwg.mundial.data.mapper

import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.model.MatchPrediction
private val MONTHS_ES = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

fun PartidoDto.toMatchPrediction(): MatchPrediction {
    val group = pais1.grupo
    return MatchPrediction(
        id = id.toString(),
        group = group,
        homeTeam = pais1.nombre,
        awayTeam = pais2.nombre,
        homeFlagUrl = pais1.bandera,
        awayFlagUrl = pais2.bandera,
        dateTime = formatPartidoDateTime(fecha, hora),
        venue = fase.nombre,
        isFinished = finalizado,
        finalHomeScore = if (finalizado) marcadorPais1 else null,
        finalAwayScore = if (finalizado) marcadorPais2 else null,
        predictionHomeScore = marcadorPais1,
        predictionAwayScore = marcadorPais2,
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
    filter { it.pais1.grupo == group }.map { it.toMatchPrediction() }

fun List<PartidoDto>.availableGroups(): List<String> =
    map { it.pais1.grupo }
        .distinct()
        .sorted()
