package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CustomDarkColorScheme = darkColorScheme(
    primary = SecOpsPrimary,
    onPrimary = SecOpsOnPrimary,
    primaryContainer = SecOpsSurfaceVariant,
    secondary = SecOpsSecondary,
    tertiary = SecOpsTertiary,
    background = SecOpsBackground,
    surface = SecOpsSurface,
    surfaceVariant = SecOpsSurfaceVariant,
    onBackground = SecOpsOnBackground,
    onSurface = SecOpsOnSurface,
    onSurfaceVariant = SecOpsOnSurfaceVariant,
    error = SecOpsError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Lock theme mode to dark design for premium hacker/matrix/kali visual vibe
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
