package com.thesunnahrevival.sunnahassistant.views.utilities

object ArabicTextUtils {
    private const val ZWSP = "\u200B"  // Zero-width space

    fun formatArabicText(text: String): String {
        return text.replace(" ", " $ZWSP")
    }
}