package com.itwg.mundial.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.itwg.mundial.ui.home.HomeMatchUi
import com.itwg.mundial.ui.theme.FinishedMatchBackground
import com.itwg.mundial.ui.theme.FinishedMatchBorder
import com.itwg.mundial.ui.theme.FinishedMatchLabel
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.util.formatPuntos

@Composable
fun HomeMatchRow(
    match: HomeMatchUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = if (match.isFinished) FinishedMatchBackground else MaterialTheme.colorScheme.surface,
            border = if (match.isFinished) {
                BorderStroke(1.dp, FinishedMatchBorder)
            } else {
                null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = match.dateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (match.isFinished) {
                        Text(
                            text = "Finalizado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FinishedMatchLabel,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeMatchTeam(
                        name = match.homeTeam,
                        flagUrl = match.homeFlagUrl,
                        alignEnd = false,
                        modifier = Modifier.weight(1f),
                    )
                    HomeMatchScores(
                        match = match,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    HomeMatchTeam(
                        name = match.awayTeam,
                        flagUrl = match.awayFlagUrl,
                        alignEnd = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = formatGanadoLabel(match),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (match.isFinished && match.ganado > 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = when {
                        match.isFinished && match.ganado > 0 -> FinishedMatchLabel
                        match.isFinished -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun HomeMatchTeam(
    name: String,
    flagUrl: String?,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Row(
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!alignEnd) {
                HomeMatchFlag(flagUrl)
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                modifier = Modifier.padding(
                    start = if (alignEnd) 0.dp else 6.dp,
                    end = if (alignEnd) 6.dp else 0.dp,
                ),
            )
            if (alignEnd) {
                HomeMatchFlag(flagUrl)
            }
        }
    }
}

@Composable
private fun HomeMatchFlag(flagUrl: String?) {
    if (flagUrl.isNullOrBlank()) return
    AsyncImage(
        model = flagUrl,
        contentDescription = null,
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun HomeMatchScores(
    match: HomeMatchUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ScoreLine(
            label = "Final",
            value = if (match.isFinished) {
                formatScorePair(match.finalHomeScore, match.finalAwayScore)
            } else {
                "—"
            },
            emphasized = match.isFinished,
            emphasizedColor = if (match.isFinished) FinishedMatchLabel else MaterialTheme.colorScheme.onSurface,
        )
        ScoreLine(
            label = "Tu marcador",
            value = if (match.predictionHomeScore != null && match.predictionAwayScore != null) {
                formatScorePair(match.predictionHomeScore, match.predictionAwayScore)
            } else {
                "—"
            },
            emphasized = false,
            emphasizedColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ScoreLine(
    label: String,
    value: String,
    emphasized: Boolean,
    emphasizedColor: androidx.compose.ui.graphics.Color,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = value,
        style = if (emphasized) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        color = if (emphasized) emphasizedColor else MaterialTheme.colorScheme.onSurface,
    )
}

private fun formatScorePair(home: Int?, away: Int?): String =
    "${home ?: 0} - ${away ?: 0}"

private fun formatGanadoLabel(match: HomeMatchUi): String = when {
    !match.isFinished -> "Ganaste: pendiente"
    match.ganado > 0 -> "Ganaste: +${formatPuntos(match.ganado)} pts"
    else -> "Ganaste: ${formatPuntos(match.ganado)} pts"
}

@Preview(showBackground = true)
@Composable
private fun HomeMatchRowPreview() {
    MundialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeMatchRow(
                onClick = {},
                match = HomeMatchUi(
                    id = "1",
                    group = "A",
                    homeTeam = "México",
                    awayTeam = "Sudáfrica",
                    homeFlagUrl = null,
                    awayFlagUrl = null,
                    dateTime = "11 jun • 13:00",
                    isFinished = false,
                    finalHomeScore = null,
                    finalAwayScore = null,
                    predictionHomeScore = 2,
                    predictionAwayScore = 1,
                    ganado = 0.0,
                ),
            )
            HomeMatchRow(
                onClick = {},
                match = HomeMatchUi(
                    id = "2",
                    group = "B",
                    homeTeam = "Brasil",
                    awayTeam = "Marruecos",
                    homeFlagUrl = null,
                    awayFlagUrl = null,
                    dateTime = "13 jun • 16:00",
                    isFinished = true,
                    finalHomeScore = 2,
                    finalAwayScore = 1,
                    predictionHomeScore = 2,
                    predictionAwayScore = 1,
                    ganado = 2.5,
                ),
                showDivider = false,
            )
        }
    }
}
