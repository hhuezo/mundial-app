package com.itwg.mundial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.itwg.mundial.model.MatchPrediction
import com.itwg.mundial.model.resolveMatchStatus
import com.itwg.mundial.model.stripeColor
import com.itwg.mundial.ui.theme.Linen
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.Pearl
import com.itwg.mundial.ui.theme.Sand

@Composable
fun MatchPredictionCard(
    match: MatchPrediction,
    homeScore: String,
    awayScore: String,
    onHomeScoreChange: (String) -> Unit,
    onAwayScoreChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = resolveMatchStatus(match, homeScore, awayScore)
    val stripeColor = status.stripeColor()
    val displayHome = if (match.isFinished) {
        match.finalHomeScore?.toString() ?: "-"
    } else {
        homeScore
    }
    val displayAway = if (match.isFinished) {
        match.finalAwayScore?.toString() ?: "-"
    } else {
        awayScore
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(stripeColor),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = Linen,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = match.dateTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TeamColumn(
                        name = match.homeTeam,
                        flagUrl = match.homeFlagUrl,
                        modifier = Modifier.weight(1f),
                    )
                    ScoreSection(
                        homeScore = displayHome,
                        awayScore = displayAway,
                        isFinished = match.isFinished,
                        onHomeScoreChange = onHomeScoreChange,
                        onAwayScoreChange = onAwayScoreChange,
                    )
                    TeamColumn(
                        name = match.awayTeam,
                        flagUrl = match.awayFlagUrl,
                        modifier = Modifier.weight(1f),
                        alignEnd = true,
                    )
                }

                HorizontalDivider(color = Sand.copy(alpha = 0.6f))
                Text(
                    text = match.venue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(
    name: String,
    flagUrl: String?,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TeamFlag(name = name, flagUrl = flagUrl)
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
    }
}

@Composable
private fun TeamFlag(
    name: String,
    flagUrl: String?,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Linen)
            .border(1.dp, Sand, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!flagUrl.isNullOrBlank()) {
            AsyncImage(
                model = flagUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = name.take(3).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ScoreSection(
    homeScore: String,
    awayScore: String,
    isFinished: Boolean,
    onHomeScoreChange: (String) -> Unit,
    onAwayScoreChange: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isFinished) {
            FinishedScoreBox(score = homeScore)
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            FinishedScoreBox(score = awayScore)
        } else {
            ScoreInput(value = homeScore, onValueChange = onHomeScoreChange)
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            ScoreInput(value = awayScore, onValueChange = onAwayScoreChange)
        }
    }
}

@Composable
private fun ScoreInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (new.length <= 2 && (new.isEmpty() || new.all { it.isDigit() })) {
                onValueChange(new)
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Linen)
            .border(
                width = 1.dp,
                color = Sand,
                shape = RoundedCornerShape(12.dp),
            ),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun FinishedScoreBox(score: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Linen),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = score,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchPredictionCardPreview() {
    MundialTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MatchPredictionCard(
                match = MatchPrediction(
                    id = "1",
                    group = "A",
                    homeTeam = "México",
                    awayTeam = "EE.UU.",
                    homeFlagUrl = "https://flagcdn.com/w320/mx.png",
                    awayFlagUrl = "https://flagcdn.com/w320/us.png",
                    dateTime = "12 jun • 18:00",
                    venue = "Estadio Azteca",
                ),
                homeScore = "2",
                awayScore = "1",
                onHomeScoreChange = {},
                onAwayScoreChange = {},
            )
            MatchPredictionCard(
                match = MatchPrediction(
                    id = "2",
                    group = "A",
                    homeTeam = "Canadá",
                    awayTeam = "Argentina",
                    dateTime = "13 jun • 21:00",
                    venue = "BC Place",
                    isFinished = true,
                    finalHomeScore = 1,
                    finalAwayScore = 2,
                ),
                homeScore = "",
                awayScore = "",
                onHomeScoreChange = {},
                onAwayScoreChange = {},
            )
        }
    }
}
