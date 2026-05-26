package com.itwg.mundial.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itwg.mundial.model.MatchPrediction
import com.itwg.mundial.ui.components.EditMatchScoreDialog
import com.itwg.mundial.ui.components.HomeFacesCarousel
import com.itwg.mundial.ui.components.HomeGroupChips
import com.itwg.mundial.ui.components.MatchPredictionCard
import com.itwg.mundial.ui.marcadores.MarcadoresViewModel
import com.itwg.mundial.ui.marcadores.MarcadoresViewModelFactory
import com.itwg.mundial.ui.marcadores.toCarouselFace
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
    unidadId: Long?,
    modifier: Modifier = Modifier,
) {
    val viewModel: MarcadoresViewModel = viewModel(
        key = "marcadores_$userId",
        factory = MarcadoresViewModelFactory(userId, unidadId),
    )
    val uiState by viewModel.uiState.collectAsState()
    val scoreState = remember(userId) { mutableStateMapOf<String, Pair<String, String>>() }
    var editingMatch by remember { mutableStateOf<MatchPrediction?>(null) }

    LaunchedEffect(userId) {
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

    val selectedFace = uiState.faces.find { it.id == uiState.selectedFaceId }
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
                    Text("Reintentar")
                }
            }
        }
        uiState.faces.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No hay partidos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            editingMatch?.let { match ->
                if (!match.isFinished) {
                    DisposableEffect(match.id) {
                        viewModel.clearSaveMarcadorError()
                        onDispose { }
                    }
                    val scores = scoreState[match.id]
                        ?: (match.predictionHomeScore?.toString().orEmpty() to
                            match.predictionAwayScore?.toString().orEmpty())
                    EditMatchScoreDialog(
                        match = match,
                        homeScore = scores.first,
                        awayScore = scores.second,
                        onHomeScoreChange = { newHome ->
                            val current = scoreState[match.id] ?: ("" to "")
                            scoreState[match.id] = newHome to current.second
                        },
                        onAwayScoreChange = { newAway ->
                            val current = scoreState[match.id] ?: ("" to "")
                            scoreState[match.id] = current.first to newAway
                        },
                        onDismiss = {
                            if (!uiState.isSavingMarcador) {
                                editingMatch = null
                            }
                        },
                        onConfirm = {
                            val current = scoreState[match.id] ?: ("" to "")
                            viewModel.saveMarcador(
                                partidoId = match.id,
                                homeScore = current.first,
                                awayScore = current.second,
                                onSuccess = { editingMatch = null },
                            )
                        },
                        isSaving = uiState.isSavingMarcador,
                        errorMessage = uiState.saveMarcadorError,
                    )
                }
            }

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
                    Text(
                        text = "Fase",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    HomeFacesCarousel(
                        faces = uiState.faces.map { it.toCarouselFace() },
                        selectedFaceId = uiState.selectedFaceId,
                        onFaceSelected = viewModel::selectFace,
                    )
                }

                if (selectedFace?.isGroupPhase == true && uiState.selectedGroup != null) {
                    item {
                        HomeGroupChips(
                            groups = selectedFace.groups,
                            selectedGroup = uiState.selectedGroup!!,
                            onGroupSelected = viewModel::selectGroup,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                if (uiState.matches.isEmpty()) {
                    item {
                        Text(
                            text = if (selectedFace?.isGroupPhase == true) {
                                "No hay partidos en este grupo."
                            } else {
                                "No hay partidos en esta fase."
                            },
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
                            onEditClick = { editingMatch = match },
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
                label = "Sin ingresar",
            )
            LegendItem(
                color = MarkerEntered,
                label = "Ingresado",
            )
            LegendItem(
                color = MarkerFinished,
                label = "Finalizado",
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
        MarcadoresScreen(userId = 1L, unidadId = 1L)
    }
}
