package com.itwg.mundial.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itwg.mundial.ui.home.HomeUsuarioUi
import com.itwg.mundial.ui.home.HomeViewMode
import com.itwg.mundial.ui.theme.AntiqueGold
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.Pearl
import com.itwg.mundial.util.formatDinero

@Composable
fun HomeViewModeSelector(
    selectedMode: HomeViewMode,
    onModeSelected: (HomeViewMode) -> Unit,
    showRankingOption: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!showRankingOption) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HomeViewModeSegment(
                text = "Mis partidos",
                icon = Icons.Outlined.SportsSoccer,
                selected = selectedMode == HomeViewMode.MY_MATCHES,
                onClick = { onModeSelected(HomeViewMode.MY_MATCHES) },
                modifier = Modifier.weight(1f),
            )
            HomeViewModeSegment(
                text = "Ranking",
                icon = Icons.Outlined.EmojiEvents,
                selected = selectedMode == HomeViewMode.UNIT_RANKING,
                onClick = { onModeSelected(HomeViewMode.UNIT_RANKING) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeViewModeSegment(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Midnight else Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Pearl else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Pearl else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}



@Composable

fun HomeUnitRankingList(

    faseNombre: String,

    usuarios: List<HomeUsuarioUi>,

    currentUserId: Long,

    modifier: Modifier = Modifier,

) {

    if (usuarios.isEmpty()) {

        Text(

            text = "No hay jugadores en esta fase.",

            style = MaterialTheme.typography.bodyMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant,

            modifier = modifier.padding(vertical = 8.dp),

        )

        return

    }



    Column(

        modifier = modifier.fillMaxWidth(),

        verticalArrangement = Arrangement.spacedBy(8.dp),

    ) {

        Text(

            text = "Ganancias en $faseNombre",

            style = MaterialTheme.typography.titleSmall,

            color = MaterialTheme.colorScheme.onSurface,

            modifier = Modifier.padding(bottom = 4.dp),

        )



        usuarios.forEachIndexed { index, usuario ->

            HomeUnitUserCard(

                position = index + 1,

                usuario = usuario,

                isCurrentUser = usuario.id == currentUserId,

            )

        }

    }

}



@Composable

private fun HomeUnitUserCard(

    position: Int,

    usuario: HomeUsuarioUi,

    isCurrentUser: Boolean,

) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(containerColor = Pearl),

        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp),

        border = if (isCurrentUser) BorderStroke(1.5.dp, Midnight) else null,

    ) {

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(horizontal = 14.dp, vertical = 12.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(12.dp),

        ) {

            RankingPositionBadge(position = position)



            Column(modifier = Modifier.weight(1f)) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.spacedBy(6.dp),

                ) {

                    Text(

                        text = usuario.name,

                        style = MaterialTheme.typography.bodyLarge,

                        fontWeight = FontWeight.SemiBold,

                        color = MaterialTheme.colorScheme.onSurface,

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

                    text = formatDinero(usuario.dineroGanado),

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color = if (usuario.dineroGanado > 0) AntiqueGold else MaterialTheme.colorScheme.onSurface,

                )

            }

        }

    }

}



@Composable

private fun RankingPositionBadge(position: Int) {

    Surface(

        modifier = Modifier.size(32.dp),

        shape = CircleShape,

        color = if (position <= 3) Midnight else MaterialTheme.colorScheme.surfaceVariant,

    ) {

        Box(contentAlignment = Alignment.Center) {

            Text(

                text = position.toString(),

                style = MaterialTheme.typography.labelLarge,

                fontWeight = FontWeight.Bold,

                color = if (position <= 3) Pearl else MaterialTheme.colorScheme.onSurfaceVariant,

            )

        }

    }

}



@Preview(showBackground = true)

@Composable

private fun HomeUnitRankingPreview() {

    MundialTheme {

        Column(modifier = Modifier.padding(16.dp)) {

            HomeUnitRankingList(

                faseNombre = "Fase de grupos",

                usuarios = listOf(

                    HomeUsuarioUi(1, "Ana", "ana@test.com", 15.5, 155.0),

                    HomeUsuarioUi(2, "Juan", "juan@test.com", 12.0, 120.0),

                ),

                currentUserId = 2L,

            )

        }

    }

}

