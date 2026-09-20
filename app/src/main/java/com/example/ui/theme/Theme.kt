package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GlowCyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003644),
    onPrimaryContainer = GlowCyanLight,
    secondary = GlowRoseSecondary,
    onSecondary = Color.Black,
    tertiary = GlowPeachAccent,
    background = DeepSpaceNight,
    surface = SurfaceCardDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E2640),
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007799),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F4FF),
    onPrimaryContainer = Color(0xFF00202A),
    secondary = Color(0xFFD84A40),
    onSecondary = Color.White,
    tertiary = Color(0xFFE67E22),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
