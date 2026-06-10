package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberAccent,
    background = CyberBackground,
    surface = CyberSurface,
    error = CyberError,
    onPrimary = CyberOnPrimary,
    onSecondary = Color.White,
    onBackground = CyberOnBackground,
    onSurface = CyberOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberAccent,
    background = CyberBackground,
    surface = CyberSurface,
    error = CyberError,
    onPrimary = CyberOnPrimary,
    onSecondary = Color.White,
    onBackground = CyberOnBackground,
    onSurface = CyberOnSurface
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled only if wanted, default to false to protect premium theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> DarkColorScheme // Force our dark neon arcade theme for the optimal visual game experience!
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
