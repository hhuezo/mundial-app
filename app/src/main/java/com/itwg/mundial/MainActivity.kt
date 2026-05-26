package com.itwg.mundial

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.itwg.mundial.auth.BiometricAvailability
import com.itwg.mundial.auth.BiometricHelper
import com.itwg.mundial.data.repository.AuthRepository
import com.itwg.mundial.ui.auth.LoginViewModel
import com.itwg.mundial.ui.auth.LoginViewModelFactory
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.itwg.mundial.ui.screens.BiometricUnlockScreen
import com.itwg.mundial.ui.screens.HomeScreen
import com.itwg.mundial.ui.screens.LoginScreen
import com.itwg.mundial.ui.screens.MarcadoresScreen
import com.itwg.mundial.ui.screens.OpcionesScreen
import com.itwg.mundial.ui.theme.Midnight
import com.itwg.mundial.ui.theme.MidnightDark
import com.itwg.mundial.ui.theme.MundialTheme
import com.itwg.mundial.ui.theme.Pearl

/**
 * [FragmentActivity] es necesario para [BiometricPrompt] (huella), no un Fragment.
 * Sigue siendo compatible con Compose vía [setContent].
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MundialTheme {
                AppRoot()
            }
        }
    }
}

private enum class AuthGate {
    LOADING,
    BIOMETRIC_LOCK,
    LOGIN,
    LOGGED_IN,
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val authRepository = remember { AuthRepository(context.applicationContext) }
    val biometricHelper = remember(activity) { BiometricHelper(activity) }
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authRepository),
    )
    val loginUiState by loginViewModel.uiState.collectAsState()
    var authGate by rememberSaveable { mutableStateOf(AuthGate.LOADING) }
    var biometricError by rememberSaveable { mutableStateOf<String?>(null) }
    var loginBiometricError by rememberSaveable { mutableStateOf<String?>(null) }
    var biometricEnabled by rememberSaveable { mutableStateOf(false) }
    var hasStoredCredentials by remember { mutableStateOf(false) }
    var deviceBiometricAvailable by remember { mutableStateOf(false) }
    var opcionesBiometricMessage by remember { mutableStateOf<String?>(null) }
    var loggedInUserId by remember { mutableStateOf<Long?>(null) }
    var loggedInUserName by remember { mutableStateOf<String?>(null) }
    var loggedInUnidadId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refreshLoggedInSession() {
        val session = authRepository.getSession()
        val profile = authRepository.getStoredUserProfile()
        loggedInUserId = session?.userId ?: profile?.userId
        loggedInUserName = session?.userName ?: profile?.userName
        loggedInUnidadId = session?.unidadId ?: profile?.unidadId
    }

    val canLoginWithBiometric =
        deviceBiometricAvailable && hasStoredCredentials && !loginUiState.isLoading

    fun unlockWithBiometric(
        onSuccess: () -> Unit,
        reportErrorOnLogin: Boolean = false,
    ) {
        biometricHelper.authenticate(
            onSuccess = {
                scope.launch {
                    loginBiometricError = null
                    biometricError = null
                    authRepository.loginWithStoredCredentials().fold(
                        onSuccess = {
                            onSuccess()
                        },
                        onFailure = { error ->
                            val message = error.message
                                ?: "No se pudo verificar la huella."
                            biometricError = message
                            if (reportErrorOnLogin) {
                                loginBiometricError = message
                            }
                        },
                    )
                }
            },
            onError = { message ->
                biometricError = message
                if (reportErrorOnLogin) {
                    loginBiometricError = message
                }
            },
            onCancel = {
                biometricError = null
                loginBiometricError = null
            },
        )
    }

    LaunchedEffect(Unit) {
        if (authGate == AuthGate.LOADING) {
            biometricEnabled = authRepository.isBiometricEnabled()
            hasStoredCredentials = authRepository.hasStoredCredentials()
            deviceBiometricAvailable =
                biometricHelper.availability() == BiometricAvailability.Available
            authGate = when {
                authRepository.shouldShowBiometricLock() -> AuthGate.BIOMETRIC_LOCK
                authRepository.hasActiveToken() -> {
                    authRepository.restoreSession()
                    AuthGate.LOGGED_IN
                }
                else -> AuthGate.LOGIN
            }
        }
    }

    LaunchedEffect(authGate) {
        when (authGate) {
            AuthGate.BIOMETRIC_LOCK -> {
                unlockWithBiometric(onSuccess = { authGate = AuthGate.LOGGED_IN })
            }
            AuthGate.LOGIN -> {
                hasStoredCredentials = authRepository.hasStoredCredentials()
                loginBiometricError = null
            }
            else -> Unit
        }
    }

    LaunchedEffect(authGate) {
        if (authGate == AuthGate.LOGGED_IN) {
            refreshLoggedInSession()
        }
    }

    LaunchedEffect(loginUiState.loginSuccess) {
        if (loginUiState.loginSuccess) {
            authGate = AuthGate.LOGGED_IN
            hasStoredCredentials = true
            biometricEnabled = authRepository.isBiometricEnabled()
            refreshLoggedInSession()
            loginViewModel.consumeLoginSuccess()
        }
    }

    val isLoggedIn = authGate == AuthGate.LOGGED_IN
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isLoggedIn
            insetsController.isAppearanceLightNavigationBars = isLoggedIn
        }
    }

    when (authGate) {
        AuthGate.LOADING -> Unit
        AuthGate.BIOMETRIC_LOCK -> BiometricUnlockScreen(
            onUnlockWithBiometric = {
                unlockWithBiometric(onSuccess = { authGate = AuthGate.LOGGED_IN })
            },
            onUsePassword = { authGate = AuthGate.LOGIN },
            errorMessage = biometricError,
        )
        AuthGate.LOGIN -> LoginScreen(
            onLoginClick = { email, password ->
                loginBiometricError = null
                loginViewModel.login(email, password)
            },
            isLoading = loginUiState.isLoading,
            errorMessage = loginBiometricError ?: loginUiState.errorMessage,
            showBiometricButton = canLoginWithBiometric,
            onBiometricClick = {
                unlockWithBiometric(
                    onSuccess = { authGate = AuthGate.LOGGED_IN },
                    reportErrorOnLogin = true,
                )
            },
        )
        AuthGate.LOGGED_IN -> {
            val userId = loggedInUserId
            if (userId != null) {
                MundialApp(
                    userId = userId,
                    userName = loggedInUserName,
                    unidadId = loggedInUnidadId,
                    onProfileRefreshed = { id, name, unidad ->
                        loggedInUserId = id
                        loggedInUserName = name
                        loggedInUnidadId = unidad
                    },
                    biometricEnabled = biometricEnabled,
                    onBiometricToggle = { enabled ->
                        opcionesBiometricMessage = null
                        if (enabled) {
                            scope.launch {
                                if (!authRepository.hasStoredCredentials()) {
                                    opcionesBiometricMessage =
                                        "Inicia sesión con correo antes de activar la huella."
                                    return@launch
                                }
                                when (biometricHelper.availability()) {
                                    BiometricAvailability.Available -> {
                                        biometricHelper.authenticate(
                                            onSuccess = {
                                                scope.launch {
                                                    authRepository.setBiometricEnabled(true)
                                                    biometricEnabled = true
                                                }
                                            },
                                            onError = { msg -> opcionesBiometricMessage = msg },
                                        )
                                    }
                                    BiometricAvailability.NoHardware -> {
                                        opcionesBiometricMessage =
                                            "Este dispositivo no tiene lector de huella."
                                    }
                                    BiometricAvailability.NotEnrolled -> {
                                        opcionesBiometricMessage =
                                            "Registra una huella en Ajustes del teléfono primero."
                                    }
                                    BiometricAvailability.Unavailable -> {
                                        opcionesBiometricMessage =
                                            "No se pudo verificar la huella."
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                authRepository.setBiometricEnabled(false)
                                biometricEnabled = false
                            }
                        }
                    },
                    biometricMessage = opcionesBiometricMessage,
                    onLogout = {
                        scope.launch {
                            authRepository.lockSession()
                            hasStoredCredentials = authRepository.hasStoredCredentials()
                            deviceBiometricAvailable =
                                biometricHelper.availability() == BiometricAvailability.Available
                            loginBiometricError = null
                            biometricError = null
                            authGate = AuthGate.LOGIN
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun MundialApp(
    userId: Long,
    userName: String? = null,
    unidadId: Long? = null,
    onProfileRefreshed: (userId: Long, userName: String?, unidadId: Long?) -> Unit = { _, _, _ -> },
    biometricEnabled: Boolean = false,
    onBiometricToggle: (Boolean) -> Unit = {},
    biometricMessage: String? = null,
    onLogout: () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var partidoDetailOnBack by remember { mutableStateOf<(() -> Unit)?>(null) }
    val isPartidoDetail = partidoDetailOnBack != null

    BackHandler(enabled = isPartidoDetail) {
        partidoDetailOnBack?.invoke()
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label,
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    modifier = Modifier.height(if (isPartidoDetail) 72.dp else 64.dp),
                    navigationIcon = {
                        if (isPartidoDetail) {
                            IconButton(onClick = { partidoDetailOnBack?.invoke() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Pearl,
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = if (isPartidoDetail) {
                                "Detalle del partido"
                            } else {
                                currentDestination.label
                            },
                            style = if (isPartidoDetail) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Midnight,
                        titleContentColor = Pearl,
                        navigationIconContentColor = Pearl,
                        scrolledContainerColor = MidnightDark,
                    ),
                )
            },
        ) { innerPadding ->
            val screenModifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    userId = userId,
                    userName = userName,
                    unidadId = unidadId,
                    onProfileRefreshed = onProfileRefreshed,
                    onPartidoDetailChange = { onBack ->
                        partidoDetailOnBack = onBack
                    },
                    modifier = screenModifier,
                )
                AppDestinations.MARCADORES -> MarcadoresScreen(
                    userId = userId,
                    unidadId = unidadId,
                    modifier = screenModifier,
                )
                AppDestinations.OPCIONES -> OpcionesScreen(
                    userId = userId,
                    modifier = screenModifier,
                    biometricEnabled = biometricEnabled,
                    onBiometricToggle = onBiometricToggle,
                    biometricMessage = biometricMessage,
                    onLogout = onLogout,
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    MARCADORES("Marcadores", Icons.Default.List),
    OPCIONES("Opciones", Icons.Default.Settings),
}
