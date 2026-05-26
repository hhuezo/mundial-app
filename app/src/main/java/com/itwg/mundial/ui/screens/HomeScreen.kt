package com.itwg.mundial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.itwg.mundial.data.mapper.filterHomeMatchesByGroup
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itwg.mundial.data.repository.HomeRepository
import com.itwg.mundial.ui.components.HomeFacesCarousel
import com.itwg.mundial.ui.components.HomeGroupChips
import com.itwg.mundial.ui.components.HomeMatchRow
// import com.itwg.mundial.ui.components.HomeSelectedFaceSummary
import com.itwg.mundial.ui.components.HomeStatsCard
import com.itwg.mundial.ui.components.HomeUnitRankingList
import com.itwg.mundial.ui.components.HomeViewModeSelector
import com.itwg.mundial.ui.home.HomeViewMode
import com.itwg.mundial.ui.home.HomeViewModel
import com.itwg.mundial.ui.home.HomeViewModelFactory
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun HomeScreen(
    userId: Long,
    userName: String?,
    unidadId: Long?,
    onProfileRefreshed: (userId: Long, userName: String?, unidadId: Long?) -> Unit = { _, _, _ -> },
    onPartidoDetailChange: (onBack: (() -> Unit)?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        key = "home_$userId",
        factory = HomeViewModelFactory(
            repository = HomeRepository(context.applicationContext),
            userId = userId,
            initialUserName = userName,
            initialUnidadId = unidadId,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedFaceId by remember(userId) { mutableLongStateOf(-1L) }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPartidoId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadHome()
    }

    LaunchedEffect(uiState.isLoading, uiState.faces) {
        if (!uiState.isLoading && uiState.faces.isNotEmpty()) {
            if (selectedFaceId == -1L || uiState.faces.none { it.id == selectedFaceId }) {
                selectedFaceId = uiState.faces.first().id
            }
        }
    }

    val selectedFace = uiState.faces.find { it.id == selectedFaceId }

    LaunchedEffect(selectedFace?.id, selectedFace?.groups) {
        val face = selectedFace ?: return@LaunchedEffect
        if (face.isGroupPhase) {
            if (selectedGroup == null || selectedGroup !in face.groups) {
                selectedGroup = face.groups.first()
            }
        } else {
            selectedGroup = null
        }
    }
    val showRankingTab = uiState.hasUnitRanking

    val displayName = uiState.userName?.takeIf { it.isNotBlank() }
    val welcomeText = when {
        displayName != null -> "Bienvenido, $displayName"
        else -> "Bienvenido al Mundial"
    }

    val onMatchClick: (String) -> Unit = { matchId ->
        matchId.toLongOrNull()?.let { selectedPartidoId = it }
    }

    LaunchedEffect(selectedPartidoId) {
        onPartidoDetailChange(
            if (selectedPartidoId != null) {
                { selectedPartidoId = null }
            } else {
                null
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose { onPartidoDetailChange(null) }
    }

    selectedPartidoId?.let { partidoId ->
        key(partidoId) {
            PartidoDetailScreen(
                partidoId = partidoId,
                userId = userId,
                onBack = { selectedPartidoId = null },
                modifier = modifier,
            )
        }
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = Midnight)
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedRose,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { viewModel.loadHome() },
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
                Text(
                    text = "No hay fases disponibles.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
          

                    item(key = "view-mode") {
                        HomeViewModeSelector(
                            selectedMode = uiState.viewMode,
                            onModeSelected = viewModel::setViewMode,
                            showRankingOption = showRankingTab,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }

                    if (uiState.faces.isNotEmpty()) {
                        item(key = "carousel") {
                            Text(
                                text = "Fase",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            HomeFacesCarousel(
                                faces = uiState.faces,
                                selectedFaceId = selectedFaceId.takeIf { it >= 0 },
                                onFaceSelected = { faceId -> selectedFaceId = faceId },
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }

                    when (uiState.viewMode) {
                        HomeViewMode.UNIT_RANKING -> {
                            selectedFace?.let { face ->
                                item(key = "ranking-${face.id}") {
                                    HomeUnitRankingList(
                                        faseNombre = face.faseNombre,
                                        usuarios = face.usuarios,
                                        currentUserId = userId,
                                        modifier = Modifier.padding(top = 12.dp),
                                    )
                                }
                            }
                        }
                        HomeViewMode.MY_MATCHES -> {
                            if (uiState.faces.isNotEmpty()) {
                                selectedFace?.let { face ->
                                    /* item(key = "summary-${face.id}") {
                                        HomeSelectedFaceSummary(
                                            face = face,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                        )
                                    } */

                                    if (face.isGroupPhase && selectedGroup != null) {
                                        item(key = "groups-${face.id}") {
                                            HomeGroupChips(
                                                groups = face.groups,
                                                selectedGroup = selectedGroup!!,
                                                onGroupSelected = { selectedGroup = it },
                                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                                            )
                                        }

                                        val groupMatches = face.matches.filterHomeMatchesByGroup(selectedGroup!!)

                                        if (groupMatches.isEmpty()) {
                                            item(key = "no-group-matches-${face.id}-$selectedGroup") {
                                                Text(
                                                    text = "No hay partidos en este grupo.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                )
                                            }
                                        } else {
                                            itemsIndexed(
                                                items = groupMatches,
                                                key = { _, match -> "${face.id}-${match.id}" },
                                            ) { index, match ->
                                                HomeMatchRow(
                                                    match = match,
                                                    onClick = { onMatchClick(match.id) },
                                                    showDivider = index < groupMatches.lastIndex,
                                                )
                                            }
                                        }
                                    } else {
                                        itemsIndexed(
                                            items = face.matches,
                                            key = { _, match -> "${face.id}-${match.id}" },
                                        ) { index, match ->
                                            HomeMatchRow(
                                                match = match,
                                                onClick = { onMatchClick(match.id) },
                                                showDivider = index < face.matches.lastIndex,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MundialTheme {
        HomeScreen(userId = 1L, userName = "Juan", unidadId = 1L)
    }
}
