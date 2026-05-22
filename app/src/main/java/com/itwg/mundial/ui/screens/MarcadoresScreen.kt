package com.itwg.mundial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itwg.mundial.R
import com.itwg.mundial.ui.components.MatchPredictionCard
import com.itwg.mundial.ui.marcadores.MarcadoresViewModel
import com.itwg.mundial.ui.marcadores.MarcadoresViewModelFactory
import com.itwg.mundial.ui.theme.MarkerEntered
import com.itwg.mundial.ui.theme.MarkerFinished
import com.itwg.mundial.ui.theme.MarkerPending
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun MarcadoresScreen(
    userId: Long,
    modifier: Modifier = Modifier,
) {
    val viewModel: MarcadoresViewModel = viewModel(
        factory = MarcadoresViewModelFactory(userId),
    )
    val uiState by viewModel.uiState.collectAsState()
    val scoreState = remember { mutableStateMapOf<String, Pair<String, String>>() }

    // Recarga /marcadores cada vez que el usuario entra a esta pestaña
    LaunchedEffect(Unit) {
        viewModel.loadMarcadores()
    }

    LaunchedEffect(uiState.isLoading, uiState.matches, uiState.initialScores) {
        if (!uiState.isLoading) {
            scoreState.clear()
            uiState.matches.forEach { match ->
                val fromApi = uiState.initialScores[match.id]
                scoreState[match.id] = fromApi ?: Pair(
                    match.predictionHomeScore?.toString().orEmpty(),
                    match.predictionAwayScore?.toString().orEmpty(),
                )
            }
        }
    }

    val errorMessage = uiState.errorMessage

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Midnight)
            }
        }
        errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedRose,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { viewModel.loadMarcadores() },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Midnight,
                        contentColor = Pearl,
                    ),
                ) {
                    Text(stringResource(R.string.marcadores_retry))
                }
            }
        }
        uiState.groups.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.marcadores_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            val selectedGroup = uiState.selectedGroup ?: uiState.groups.first()
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
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
                        uiState.groups.forEach { group ->
                            FilterChip(
                                selected = selectedGroup == group,
                                onClick = { viewModel.selectGroup(group) },
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

                if (uiState.matches.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.marcadores_no_matches_group),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                } else {
                    items(uiState.matches, key = { it.id }) { match ->
                        val scores = scoreState[match.id]
                            ?: (match.predictionHomeScore?.toString().orEmpty() to
                                match.predictionAwayScore?.toString().orEmpty())
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
private fun LegendItem(color: Color, label: String) {
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
        MarcadoresScreen(userId = 1L)
    }
}
