package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GeminiDarkColorScheme = darkColorScheme(
    primary = GeminiAccent,
    onPrimary = GeminiBg,
    primaryContainer = GeminiSurface,
    onPrimaryContainer = GeminiAccent,
    secondary = GeminiAccentVariant,
    onSecondary = GeminiBg,
    background = GeminiBg,
    onBackground = GeminiText,
    surface = GeminiSurface,
    onSurface = GeminiText,
    surfaceVariant = GeminiSidebar,
    onSurfaceVariant = GeminiSubtext,
    outline = GeminiBorder,
    outlineVariant = GeminiBorder,
    error = GeminiError,
    onError = GeminiBg
)

@Composable
fun DharmeswarAiTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GeminiBg.toArgb()
            window.navigationBarColor = GeminiBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = GeminiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
