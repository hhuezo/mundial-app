package com.itwg.mundial.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.itwg.mundial.data.repository.MarcadoresRepository
import com.itwg.mundial.ui.marcadores.PartidoDetailUi
import com.itwg.mundial.ui.marcadores.PartidoDetailViewModel
import com.itwg.mundial.ui.marcadores.PartidoDetailViewModelFactory
import com.itwg.mundial.ui.marcadores.PartidoUsuarioMarcadorUi
import com.itwg.mundial.ui.theme.AntiqueGold
import com.itwg.mundial.ui.theme.FinishedMatchBackground
import com.itwg.mundial.ui.theme.FinishedMatchBorder
import com.itwg.mundial.ui.theme.FinishedMatchLabel
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl
import com.itwg.mundial.util.formatDinero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoDetailScreen(
    partidoId: Long,
    userId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember(context) { MarcadoresRepository(context.applicationContext) }
    val viewModel: PartidoDetailViewModel = viewModel(
        key = "partido_detail_${partidoId}_$userId",
        factory = PartidoDetailViewModelFactory(partidoId, userId, repository),
    )
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalle del partido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Pearl,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Midnight,
                    titleContentColor = Pearl,
                    navigationIconContentColor = Pearl,
                ),
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Midnight)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                        onClick = { viewModel.loadDetail() },
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
            uiState.detail != null -> {
                PartidoDetailContent(
                    detail = uiState.detail!!,
                    currentUserId = userId,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun PartidoDetailContent(
    detail: PartidoDetailUi,
    currentUserId: Long,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PartidoDetailHeader(detail = detail)
        }

        item {
            Text(
                text = "Marcadores de la unidad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${detail.usuarios.size} jugadores",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        if (detail.usuarios.isEmpty()) {
            item {
                Text(
                    text = "No hay jugadores en esta unidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(detail.usuarios, key = { it.id }) { usuario ->
                PartidoUsuarioMarcadorRow(
                    usuario = usuario,
                    isCurrentUser = usuario.id == currentUserId,
                    isFinished = detail.isFinished,
                )
            }
        }
    }
}

@Composable
private fun PartidoDetailHeader(detail: PartidoDetailUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (detail.isFinished) FinishedMatchBackground else Pearl,
        ),
        border = if (detail.isFinished) BorderStroke(1.dp, FinishedMatchBorder) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = detail.faseNombre,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Midnight,
                    )
                    detail.grupoLabel?.let { grupo ->
                        Text(
                            text = grupo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = detail.dateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PartidoDetailTeam(
                    name = detail.homeTeam,
                    flagUrl = detail.homeFlagUrl,
                    alignEnd = false,
                    modifier = Modifier.weight(1f),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = if (detail.isFinished) "Resultado" else "vs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (detail.isFinished) {
                            "${detail.finalHomeScore ?: 0} - ${detail.finalAwayScore ?: 0}"
                        } else {
                            "—"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (detail.isFinished) FinishedMatchLabel else MaterialTheme.colorScheme.onSurface,
                    )
                }
                PartidoDetailTeam(
                    name = detail.awayTeam,
                    flagUrl = detail.awayFlagUrl,
                    alignEnd = true,
                    modifier = Modifier.weight(1f),
                )
            }

            if (detail.isFinished) {
                Text(
                    text = "Partido finalizado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FinishedMatchLabel,
                )
            }
        }
    }
}

@Composable
private fun PartidoDetailTeam(
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        ) {
            if (!alignEnd && !flagUrl.isNullOrBlank()) {
                PartidoDetailFlag(flagUrl)
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
            if (alignEnd && !flagUrl.isNullOrBlank()) {
                PartidoDetailFlag(flagUrl)
            }
        }
    }
}

@Composable
private fun PartidoDetailFlag(flagUrl: String) {
    AsyncImage(
        model = flagUrl,
        contentDescription = null,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun PartidoUsuarioMarcadorRow(
    usuario: PartidoUsuarioMarcadorUi,
    isCurrentUser: Boolean,
    isFinished: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp),
        border = if (isCurrentUser) BorderStroke(1.5.dp, Midnight) else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = usuario.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isCurrentUser) {
                            Surface(
                                color = Midnight,
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "Tú",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Pearl,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (usuario.hasPrediction) {
                            "${usuario.marcadorHome} - ${usuario.marcadorAway}"
                        } else {
                            "Sin marcar"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (usuario.hasPrediction) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    if (isFinished) {
                        Text(
                            text = if (usuario.ganado > 0) {
                                "+${formatDinero(usuario.ganado)}"
                            } else {
                                formatDinero(usuario.ganado)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (usuario.ganado > 0) AntiqueGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else if (usuario.hasPrediction) {
                        Text(
                            text = "Marcador ingresado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
