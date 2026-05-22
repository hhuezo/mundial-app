package com.itwg.mundial.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Midnight,
    onPrimary = Pearl,
    primaryContainer = SlateMist,
    onPrimaryContainer = MidnightDark,
    secondary = WarmStone,
    onSecondary = Pearl,
    secondaryContainer = StoneMist,
    onSecondaryContainer = WarmStoneDark,
    tertiary = AntiqueGold,
    onTertiary = Pearl,
    tertiaryContainer = GoldMist,
    onTertiaryContainer = AntiqueGoldDark,
    error = MutedRose,
    onError = Pearl,
    errorContainer = RoseMist,
    onErrorContainer = MutedRose,
    background = Ivory,
    onBackground = Charcoal,
    surface = Pearl,
    onSurface = Charcoal,
    surfaceVariant = Linen,
    onSurfaceVariant = Graphite,
    outline = Pebble,
    outlineVariant = Sand,
    scrim = Charcoal,
    inverseSurface = MidnightDark,
    inverseOnSurface = Pearl,
    inversePrimary = SlateMist,
)

private val DarkColorScheme = darkColorScheme(
    primary = SlateMist,
    onPrimary = MidnightDark,
    primaryContainer = Midnight,
    onPrimaryContainer = SlateMist,
    secondary = StoneMist,
    onSecondary = WarmStoneDark,
    secondaryContainer = WarmStone,
    onSecondaryContainer = StoneMist,
    tertiary = AntiqueGold,
    onTertiary = AntiqueGoldDark,
    tertiaryContainer = AntiqueGoldDark,
    onTertiaryContainer = GoldMist,
    error = RoseMist,
    onError = MutedRose,
    errorContainer = MutedRose,
    onErrorContainer = RoseMist,
    background = MidnightDark,
    onBackground = Pearl,
    surface = Midnight,
    onSurface = Pearl,
    surfaceVariant = WarmStoneDark,
    onSurfaceVariant = Sand,
    outline = Graphite,
    outlineVariant = WarmStone,
)

@Composable
fun MundialTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && !darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(LocalContext.current)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
