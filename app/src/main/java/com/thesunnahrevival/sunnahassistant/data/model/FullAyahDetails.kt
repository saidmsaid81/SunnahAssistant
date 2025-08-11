package com.thesunnahrevival.sunnahassistant.data.model

data class AyahTranslationWithFootnotes(
    val ayahTranslation: AyahTranslation,
    val translation: Translation
)

data class FullAyahDetails(
    val ayah: Ayah,
    val surah: Surah,
    val ayahTranslations: List<AyahTranslationWithFootnotes>
)