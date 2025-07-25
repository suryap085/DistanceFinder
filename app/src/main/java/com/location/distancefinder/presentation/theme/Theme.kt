package com.location.distancefinder.presentation.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E6091),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF50606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E4F6),
    onSecondaryContainer = Color(0xFF0B1D2A),
    tertiary = Color(0xFF65587A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEBDDFF),
    onTertiaryContainer = Color(0xFF201133),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDEE3EA),
    onSurfaceVariant = Color(0xFF42484D),
    outline = Color(0xFF72787E),
    inverseOnSurface = Color(0xFFF0F1F5),
    inverseSurface = Color(0xFF2E3135),
    inversePrimary = Color(0xFF8DCFFF),
    surfaceTint = Color(0xFF1E6091),
    outlineVariant = Color(0xFFC2C8CE),
    scrim = Color(0xFF000000),
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DCFFF),
    onPrimary = Color(0xFF003450),
    primaryContainer = Color(0xFF004A70),
    onPrimaryContainer = Color(0xFFC8E6FF),
    secondary = Color(0xFFB7C8DA),
    onSecondary = Color(0xFF22323F),
    secondaryContainer = Color(0xFF384956),
    onSecondaryContainer = Color(0xFFD3E4F6),
    tertiary = Color(0xFFCFC1E8),
    onTertiary = Color(0xFF362749),
    tertiaryContainer = Color(0xFF4D3F61),
    onTertiaryContainer = Color(0xFFEBDDFF),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE1E2E6),
    surface = Color(0xFF101417),
    onSurface = Color(0xFFE1E2E6),
    surfaceVariant = Color(0xFF42484D),
    onSurfaceVariant = Color(0xFFC2C8CE),
    outline = Color(0xFF8C9297),
    inverseOnSurface = Color(0xFF191C20),
    inverseSurface = Color(0xFFE1E2E6),
    inversePrimary = Color(0xFF1E6091),
    surfaceTint = Color(0xFF8DCFFF),
    outlineVariant = Color(0xFF42484D),
    scrim = Color(0xFF000000),
)

@Composable
fun LocationFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}