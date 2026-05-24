package com.itwg.mundial.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itwg.mundial.model.MatchPrediction
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun EditMatchScoreDialog(
    match: MatchPrediction,
    homeScore: String,
    awayScore: String,
    onHomeScoreChange: (String) -> Unit,
    onAwayScoreChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean = false,
    errorMessage: String? = null,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Editar marcador",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "${match.homeTeam} vs ${match.awayTeam}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = homeScore,
                        onValueChange = { new ->
                            if (new.length <= 2 && (new.isEmpty() || new.all { it.isDigit() })) {
                                onHomeScoreChange(new)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(match.homeTeam) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = editScoreFieldColors(),
                        enabled = !isSaving,
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = awayScore,
                        onValueChange = { new ->
                            if (new.length <= 2 && (new.isEmpty() || new.all { it.isDigit() })) {
                                onAwayScoreChange(new)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(match.awayTeam) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = editScoreFieldColors(),
                        enabled = !isSaving,
                    )
                }
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedRose,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Midnight,
                    contentColor = Pearl,
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Pearl,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp),
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar", color = Midnight)
            }
        },
    )
}

@Composable
private fun editScoreFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Midnight,
    focusedLabelColor = Midnight,
    cursorColor = Midnight,
)
