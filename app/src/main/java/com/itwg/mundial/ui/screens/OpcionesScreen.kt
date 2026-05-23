package com.itwg.mundial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun OpcionesScreen(
    biometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    biometricMessage: String?,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Opciones",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Midnight,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Iniciar sesión con huella",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Usa tu huella para acceder más rápido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = onBiometricToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Pearl,
                        checkedTrackColor = Midnight,
                        uncheckedThumbColor = Pearl,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        }

        if (!biometricMessage.isNullOrBlank()) {
            Text(
                text = biometricMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MutedRose,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MutedRose,
                )
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedRose,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OpcionesScreenPreview() {
    MundialTheme {
        OpcionesScreen(
            biometricEnabled = true,
            onBiometricToggle = {},
            biometricMessage = null,
        )
    }
}
