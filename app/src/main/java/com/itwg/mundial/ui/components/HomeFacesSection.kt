package com.itwg.mundial.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itwg.mundial.ui.home.HomeFaceUi
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.util.formatPuntos
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun HomeFacesCarousel(
    faces: List<HomeFaceUi>,
    selectedFaceId: Long?,
    onFaceSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (faces.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        faces.forEach { face ->
            val selected = face.id == selectedFaceId
            FilterChip(
                selected = selected,
                onClick = { onFaceSelected(face.id) },
                label = {
                    Text(
                        text = face.faseNombre,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Midnight,
                    selectedLabelColor = Pearl,
                ),
            )
        }
    }
}

@Composable
fun HomeSelectedFaceSummary(
    face: HomeFaceUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "${face.matches.size} partidos · ${face.valor} c/u · Ganado: ${formatPuntos(face.totalGanado)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (face.partidosPendientes > 0) {
            Text(
                text = "${face.partidosPendientes} pendientes de marcar",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
