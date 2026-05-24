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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itwg.mundial.data.repository.HomeRepository
import com.itwg.mundial.ui.components.HomeFacesCarousel
import com.itwg.mundial.ui.components.HomeMatchRow
import com.itwg.mundial.ui.components.HomeSelectedFaceSummary
import com.itwg.mundial.ui.home.HomeViewModel
import com.itwg.mundial.ui.home.HomeViewModelFactory
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl
import com.itwg.mundial.util.formatPuntos

@Composable
fun HomeScreen(
    userId: Long,
    userName: String?,
    unidadId: Long?,
    onProfileRefreshed: (userId: Long, userName: String?, unidadId: Long?) -> Unit = { _, _, _ -> },
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

    val displayName = uiState.userName?.takeIf { it.isNotBlank() }
    val welcomeText = when {
        displayName != null -> "Bienvenido, $displayName"
        else -> "Bienvenido al Mundial"
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item(key = "header") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = welcomeText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "ID ${uiState.userId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                text = "Total ganado: ${formatPuntos(uiState.totalGanado)} · Pendientes: ${uiState.partidosPendientes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            )
                        }
                    }

                    item(key = "carousel") {
                        Text(
                            text = "Fases del torneo",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        HomeFacesCarousel(
                            faces = uiState.faces,
                            selectedFaceId = selectedFaceId.takeIf { it >= 0 },
                            onFaceSelected = { faceId -> selectedFaceId = faceId },
                        )
                    }

                    selectedFace?.let { face ->
                        item(key = "summary-${face.id}") {
                            HomeSelectedFaceSummary(
                                face = face,
                                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                            )
                        }

                        itemsIndexed(
                            items = face.matches,
                            key = { _, match -> match.id },
                        ) { index, match ->
                            HomeMatchRow(
                                match = match,
                                showDivider = index < face.matches.lastIndex,
                            )
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
