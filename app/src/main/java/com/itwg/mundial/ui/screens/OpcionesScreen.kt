package com.itwg.mundial.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itwg.mundial.data.repository.AuthRepository
import com.itwg.mundial.ui.opciones.OpcionesPasswordUiState
import com.itwg.mundial.ui.opciones.OpcionesViewModel
import com.itwg.mundial.ui.opciones.OpcionesViewModelFactory
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.MutedRose
import com.itwg.mundial.ui.theme.Pearl

@Composable
fun OpcionesScreen(
    userId: Long,
    biometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    biometricMessage: String?,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context.applicationContext) }
    val viewModel: OpcionesViewModel = viewModel(
        factory = OpcionesViewModelFactory(userId, authRepository),
    )
    val passwordState by viewModel.passwordState.collectAsState()

    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var passwordSuccessBanner by remember { mutableStateOf<String?>(null) }

    val dismissPasswordDialog = {
        if (!passwordState.isLoading) {
            showPasswordDialog = false
            viewModel.clearPasswordMessages()
        }
    }

    LaunchedEffect(passwordState.successMessage) {
        passwordState.successMessage?.let { message ->
            passwordSuccessBanner = message
            showPasswordDialog = false
            viewModel.clearPasswordMessages()
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            passwordState = passwordState,
            onDismiss = dismissPasswordDialog,
            onSubmit = { password, confirm ->
                viewModel.resetPassword(password, confirm)
            },
            onClearMessages = viewModel::clearPasswordMessages,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Opciones",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        passwordSuccessBanner?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Midnight,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPasswordDialog = true },
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
                            text = "Cambiar contraseña",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Actualiza la contraseña de tu cuenta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

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

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ChangePasswordDialog(
    passwordState: OpcionesPasswordUiState,
    onDismiss: () -> Unit,
    onSubmit: (password: String, confirmPassword: String) -> Unit,
    onClearMessages: () -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(passwordState.successMessage) {
        if (passwordState.successMessage != null) {
            password = ""
            confirmPassword = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        title = {
            Text(
                text = "Cambiar contraseña",
                style = MaterialTheme.typography.titleLarge,
                color = Midnight,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Ingresa tu nueva contraseña (mínimo 8 caracteres).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onClearMessages()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    enabled = !passwordState.isLoading,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Ocultar" else "Mostrar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Midnight,
                            )
                        }
                    },
                    colors = opcionesFieldColors(),
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        onClearMessages()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    enabled = !passwordState.isLoading,
                    visualTransformation = if (confirmVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSubmit(password, confirmPassword)
                        },
                    ),
                    trailingIcon = {
                        TextButton(onClick = { confirmVisible = !confirmVisible }) {
                            Text(
                                text = if (confirmVisible) "Ocultar" else "Mostrar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Midnight,
                            )
                        }
                    },
                    colors = opcionesFieldColors(),
                )

                passwordState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedRose,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(password, confirmPassword) },
                enabled = !passwordState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Midnight,
                    contentColor = Pearl,
                ),
            ) {
                if (passwordState.isLoading) {
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
            TextButton(
                onClick = onDismiss,
                enabled = !passwordState.isLoading,
            ) {
                Text("Cancelar", color = Midnight)
            }
        },
    )
}

@Composable
private fun opcionesFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Midnight,
    focusedLabelColor = Midnight,
    cursorColor = Midnight,
)

@Preview(showBackground = true)
@Composable
private fun OpcionesScreenPreview() {
    MundialTheme {
        OpcionesScreen(
            userId = 1L,
            biometricEnabled = true,
            onBiometricToggle = {},
            biometricMessage = null,
        )
    }
}
