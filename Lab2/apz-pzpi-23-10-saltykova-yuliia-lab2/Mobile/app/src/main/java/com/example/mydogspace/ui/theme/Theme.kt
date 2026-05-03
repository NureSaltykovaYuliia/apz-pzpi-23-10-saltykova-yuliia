package com.example.mydogspace.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    secondary = SecondaryCyan,
    tertiary = TertiaryYellow,
    background = BrutalBlack,
    surface = BrutalBlack,
    onPrimary = BrutalBlack,
    onSecondary = BrutalBlack,
    onTertiary = BrutalBlack,
    onBackground = BrutalWhite,
    onSurface = BrutalWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    secondary = SecondaryCyan,
    tertiary = TertiaryYellow,
    background = BrutalWhite,
    surface = BrutalWhite,
    onPrimary = BrutalWhite,
    onSecondary = BrutalBlack,
    onTertiary = BrutalBlack,
    onBackground = BrutalBlack,
    onSurface = BrutalBlack
)

@Composable
fun MyDogSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}