package com.itwg.mundial.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itwg.mundial.ui.home.HomeFaceUi
import com.itwg.mundial.ui.theme.MarkerEntered
import com.itwg.mundial.ui.theme.Midnight
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        faces.forEach { face ->
            val selected = face.id == selectedFaceId
            Surface(
                onClick = { onFaceSelected(face.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) Midnight else Pearl,
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Midnight else MaterialTheme.colorScheme.outlineVariant,
                ),
                shadowElevation = if (selected) 3.dp else 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = face.faseNombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Pearl else Midnight,
                    )
                    Text(
                        text = "${face.partidosEnFase ?: face.matches.size} partidos",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            Pearl.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun HomeGroupChips(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (groups.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Grupo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            groups.forEach { group ->
                val selected = selectedGroup == group
                Surface(
                    onClick = { onGroupSelected(group) },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MarkerEntered else Pearl,
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (selected) MarkerEntered else MaterialTheme.colorScheme.outline,
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Pearl else Midnight,
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun HomeSelectedFaceSummary(
    face: HomeFaceUi,
    modifier: Modifier = Modifier,
) {
    ...
}
*/
