package com.thesunnahrevival.sunnahassistant.views.utilities

object ArabicTextUtils {
    fun formatArabicText(text: String): String {
        return text.replace("\r\n", "\n")
    }
}
