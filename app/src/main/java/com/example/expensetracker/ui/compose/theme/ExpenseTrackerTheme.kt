package com.example.expensetracker.ui.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Purple80,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = Orange40,
    onSecondary = Orange80,
    secondaryContainer = Orange90,
    onSecondaryContainer = Orange10,
    tertiary = Pink40,
    onTertiary = Pink80,
    tertiaryContainer = Pink90,
    onTertiaryContainer = Pink10,
    error = Red40,
    onError = Red80,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Grey99,
    onBackground = Grey10,
    surface = Grey99,
    onSurface = Grey10,
    surfaceVariant = Grey90,
    onSurfaceVariant = Grey30,
    outline = Grey50,
    inverseOnSurface = Grey95,
    inverseSurface = Grey20,
    inversePrimary = Purple80,
    surfaceTint = Purple40,
    outlineVariant = Grey80,
    scrim = Grey0,
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary = Orange80,
    onSecondary = Orange20,
    secondaryContainer = Orange30,
    onSecondaryContainer = Orange90,
    tertiary = Pink80,
    onTertiary = Pink20,
    tertiaryContainer = Pink30,
    onTertiaryContainer = Pink90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Grey10,
    onBackground = Grey90,
    surface = Grey10,
    onSurface = Grey80,
    surfaceVariant = Grey30,
    onSurfaceVariant = Grey70,
    outline = Grey60,
    inverseOnSurface = Grey10,
    inverseSurface = Grey90,
    inversePrimary = Purple40,
    surfaceTint = Purple80,
    outlineVariant = Grey30,
    scrim = Grey0,
)

@Composable
fun NoNoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Tắt để dùng màu xanh lá custom
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT // Trong suốt
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
