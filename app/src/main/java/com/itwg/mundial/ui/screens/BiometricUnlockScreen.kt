package com.itwg.mundial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun BiometricUnlockScreen(
    onUnlockWithBiometric: () -> Unit,
    onUsePassword: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = Midnight,
            modifier = Modifier.height(72.dp),
        )
        Text(
            text = "Mundial",
            style = MaterialTheme.typography.headlineLarge,
            color = Midnight,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Sesión guardada. Confirma tu identidad.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MutedRose,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }

        Button(
            onClick = onUnlockWithBiometric,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Midnight,
                contentColor = Pearl,
            ),
        ) {
            Text("Iniciar con huella")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onUsePassword) {
            Text(
                text = "Usar correo y contraseña",
                color = Midnight,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BiometricUnlockScreenPreview() {
    MundialTheme {
        BiometricUnlockScreen(
            onUnlockWithBiometric = {},
            onUsePassword = {},
            errorMessage = null,
        )
    }
}
