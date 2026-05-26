package com.itwg.mundial.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.itwg.mundial.data.repository.MarcadoresRepository
import com.itwg.mundial.ui.marcadores.PartidoDetailUi
import com.itwg.mundial.ui.marcadores.PartidoDetailViewModel
import com.itwg.mundial.ui.marcadores.PartidoDetailViewModelFactory
import com.itwg.mundial.ui.marcadores.PartidoUsuarioMarcadorUi
import com.itwg.mundial.ui.theme.DetailBackground
import com.itwg.mundial.ui.theme.DetailOnPrimaryContainer
import com.itwg.mundial.ui.theme.DetailOnSecondaryContainer
import com.itwg.mundial.ui.theme.DetailOnSurfaceVariant
import com.itwg.mundial.ui.theme.DetailPrimary
import com.itwg.mundial.ui.theme.DetailPrimaryContainer
import com.itwg.mundial.ui.theme.DetailSecondary
import com.itwg.mundial.ui.theme.DetailSecondaryContainer
import com.itwg.mundial.ui.theme.DetailSurfaceContainer
import com.itwg.mundial.ui.theme.DetailTertiaryFixed
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl
import com.itwg.mundial.util.formatDinero

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

    LaunchedEffect(partidoId, userId) {
        viewModel.loadDetail()
    }

    BackHandler(onBack = onBack)

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DetailBackground),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = DetailPrimary)
            }
        }
        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(DetailBackground)
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
                        containerColor = DetailPrimary,
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
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PartidoDetailContent(
    detail: PartidoDetailUi,
    currentUserId: Long,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            PartidoDetailHeader(detail = detail)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Marcadores de la unidad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DetailPrimary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = DetailOnSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "${detail.usuarios.size} jugadores",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailOnSurfaceVariant,
                    )
                }
            }
        }

        if (detail.usuarios.isEmpty()) {
            item {
                Text(
                    text = "No hay jugadores en esta unidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DetailOnSurfaceVariant,
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
    val metaLine = buildList {
        detail.grupoLabel?.let { add(it) }
        add(detail.dateTime)
    }.joinToString(" • ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DetailPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2f, size.height * 0.35f),
                            radius = size.maxDimension * 0.75f,
                        ),
                        radius = size.maxDimension,
                        center = Offset(size.width / 2f, size.height * 0.35f),
                    )
                }
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = metaLine,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailOnPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    if (detail.isFinished) {
                        Surface(
                            color = DetailSecondary,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "Partido finalizado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    PartidoDetailTeamColumn(
                        name = detail.homeTeam,
                        flagUrl = detail.homeFlagUrl,
                    )
                    Text(
                        text = if (detail.isFinished) {
                            "${detail.finalHomeScore ?: 0} - ${detail.finalAwayScore ?: 0}"
                        } else {
                            "vs"
                        },
                        fontSize = if (detail.isFinished) 32.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                    )
                    PartidoDetailTeamColumn(
                        name = detail.awayTeam,
                        flagUrl = detail.awayFlagUrl,
                    )
                }
            }
        }
    }
}

@Composable
private fun PartidoDetailTeamColumn(
    name: String,
    flagUrl: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.widthIn(max = 110.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, DetailTertiaryFixed, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (!flagUrl.isNullOrBlank() && !LocalInspectionMode.current) {
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
                    color = DetailPrimary,
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DetailOnPrimaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PartidoUsuarioMarcadorRow(
    usuario: PartidoUsuarioMarcadorUi,
    isCurrentUser: Boolean,
    isFinished: Boolean,
) {
    val isWinner = isFinished && usuario.ganado > 0.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = if (isCurrentUser) 1.5.dp else 1.dp,
            color = if (isCurrentUser) DetailPrimary else DetailTertiaryFixed,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PartidoPlayerAvatar(
                    name = usuario.name,
                    isCurrentUser = isCurrentUser,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = usuario.name.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DetailPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isCurrentUser) {
                            Surface(
                                color = DetailPrimary,
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "Tú",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    PartidoPredictionChip(
                        usuario = usuario,
                        isFinished = isFinished,
                    )
                }
            }

            if (isFinished) {
                PartidoGanadoBadge(
                    ganado = usuario.ganado,
                    isWinner = isWinner,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PartidoPlayerAvatar(
    name: String,
    isCurrentUser: Boolean,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = if (isCurrentUser) DetailSecondaryContainer else DetailSurfaceContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = nameInitials(name),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) DetailOnSecondaryContainer else DetailPrimary,
            )
        }
    }
}

@Composable
private fun PartidoPredictionChip(
    usuario: PartidoUsuarioMarcadorUi,
    isFinished: Boolean,
) {
    if (usuario.hasPrediction) {
        Surface(
            color = DetailSurfaceContainer,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, DetailTertiaryFixed),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Predicción",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DetailOnSurfaceVariant,
                )
                Text(
                    text = "${usuario.marcadorHome} - ${usuario.marcadorAway}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = DetailPrimary,
                )
            }
        }
    } else {
        Surface(
            color = DetailSurfaceContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, DetailTertiaryFixed.copy(alpha = 0.6f)),
        ) {
            Text(
                text = if (isFinished) "Sin predicción" else "Sin marcar",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = DetailOnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PartidoGanadoBadge(
    ganado: Double,
    isWinner: Boolean,
    modifier: Modifier = Modifier,
) {
    val amountText = if (ganado > 0) {
        "+${formatDinero(ganado)}"
    } else {
        formatDinero(ganado)
    }
    Surface(
        modifier = modifier.widthIn(min = 72.dp),
        shape = RoundedCornerShape(8.dp),
        color = DetailPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isWinner) "GANÓ" else "GANADO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DetailOnPrimaryContainer,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

private fun nameInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}
