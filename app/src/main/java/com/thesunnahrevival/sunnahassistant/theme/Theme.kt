package com.thesunnahrevival.sunnahassistant.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandColor = Color(0xFFEC685B)

@Composable
fun SunnahAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColors = lightColors()
        .copy(
            primary = BrandColor,
            surface = Color(250, 250, 250)
        )
    val darkColors = darkColors()
        .copy(
            primary = BrandColor,
            surface = Color(48, 48, 48),
            onBackground = Color.White
        )
    MaterialTheme(colors = if (darkTheme) darkColors else lightColors, content = content)
}