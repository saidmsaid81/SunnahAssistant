package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AyahTranslationWithFootnotes(
    @Embedded val ayahTranslation: AyahTranslation,
    @Relation(
        parentColumn = "translation_id",
        entityColumn = "id"
    )
    val translation: Translation,
    @Relation(
        parentColumn = "id",
        entityColumn = "ayah_translation_id"
    )
    val footnotes: List<Footnote>
)

data class FullAyahDetails(
    @Embedded val ayah: Ayah,
    @Relation(
        parentColumn = "surah_id",
        entityColumn = "id"
    )
    val surah: Surah,
    @Relation(
        parentColumn = "id",
        entityColumn = "ayah_id",
        entity = AyahTranslation::class
    )
    val ayahTranslations: List<AyahTranslationWithFootnotes>
)