package com.itwg.mundial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itwg.mundial.R
import com.itwg.mundial.data.SampleMatches
import com.itwg.mundial.ui.components.MatchPredictionCard
import com.itwg.mundial.ui.theme.MarkerEntered
import com.itwg.mundial.ui.theme.MarkerFinished
import com.itwg.mundial.ui.theme.MarkerPending
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun MarcadoresScreen(modifier: Modifier = Modifier) {
    var selectedGroup by rememberSaveable { mutableStateOf(SampleMatches.groups.first()) }
    val scoreState = remember { mutableStateMapOf<String, Pair<String, String>>() }

    val matches = remember(selectedGroup) { SampleMatches.forGroup(selectedGroup) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.marcadores_predictions_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.marcadores_predictions_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            MarcadorStatusLegend()
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SampleMatches.groups.forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = {
                            Text(stringResource(R.string.marcadores_group, group))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Midnight,
                            selectedLabelColor = Pearl,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }

        items(matches, key = { it.id }) { match ->
            val scores = scoreState[match.id] ?: ("" to "")
            MatchPredictionCard(
                match = match,
                homeScore = scores.first,
                awayScore = scores.second,
                onHomeScoreChange = { scoreState[match.id] = it to scores.second },
                onAwayScoreChange = { scoreState[match.id] = scores.first to it },
            )
        }
    }
}

@Composable
private fun MarcadorStatusLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LegendItem(
                color = MarkerPending,
                label = stringResource(R.string.marcadores_status_pending),
            )
            LegendItem(
                color = MarkerEntered,
                label = stringResource(R.string.marcadores_status_entered),
            )
            LegendItem(
                color = MarkerFinished,
                label = stringResource(R.string.marcadores_status_finished),
            )
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarcadoresScreenPreview() {
    MundialTheme {
        MarcadoresScreen()
    }
}
