package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo

data class AyahTranslationWithFootnotes(
    val ayahTranslation: AyahTranslation,
    val translation: Translation
)

data class FullAyahDetails(
    val ayah: Ayah,
    val surah: Surah,
    val ayahTranslations: List<AyahTranslationWithFootnotes>
)

data class FullAyahDetailsRaw(
    @ColumnInfo(name = "ayah_id") val ayahId: Int,
    @ColumnInfo(name = "ayah_number") val ayahNumber: Int,
    @ColumnInfo(name = "ayah_surah_id") val ayahSurahId: Int,
    @ColumnInfo(name = "ayah_arabic_text") val ayahArabicText: String,
    @ColumnInfo(name = "ayah_bookmarked") val ayahBookmarked: Boolean,
    @ColumnInfo(name = "ayah_translation_id") val ayahTranslationId: Int?,
    @ColumnInfo(name = "ayah_translation_translation_id") val ayahTranslationTranslationId: Int?,
    @ColumnInfo(name = "ayah_translation_text") val ayahTranslationText: String?,
    @ColumnInfo(name = "ayah_translation_ayah_id") val ayahTranslationAyahId: Int?,
    @ColumnInfo(name = "translation_id") val translationId: Int?,
    @ColumnInfo(name = "translation_name") val translationName: String?,
    @ColumnInfo(name = "translation_language_id") val translationLanguageId: Int?,
    @ColumnInfo(name = "translation_key") val translationKey: String?,
    @ColumnInfo(name = "translation_selected") val translationSelected: Boolean?,
    @ColumnInfo(name = "translation_order") val translationOrder: Int?,
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "surah_arabic_name") val surahArabicName: String,
    @ColumnInfo(name = "surah_transliterated_name") val surahTransliteratedName: String,
    @ColumnInfo(name = "surah_is_makki") val surahIsMakki: Boolean,
    @ColumnInfo(name = "surah_verse_count") val surahVerseCount: Int,
    @ColumnInfo(name = "surah_start_page") val surahStartPage: Int,
    @ColumnInfo(name = "surah_pin_order") val surahPinOrder: Int?
)

/**
 * Extension function to convert a list of FullAyahDetailsRaw (which may contain duplicated ayahs with different translations)
 * into a list of FullAyahDetails where each ayah appears only once with all its translations grouped together.
 */
fun List<FullAyahDetailsRaw>.toGroupedFullAyahDetails(): List<FullAyahDetails> {
    return this.groupBy { it.ayahId }.map { (_, rawDetails) ->
        val firstRow = rawDetails.first()

        val ayah = Ayah(
            id = firstRow.ayahId,
            number = firstRow.ayahNumber,
            surahId = firstRow.ayahSurahId,
            arabicText = firstRow.ayahArabicText,
            bookmarked = firstRow.ayahBookmarked
        )

        val surah = Surah(
            id = firstRow.surahId,
            arabicName = firstRow.surahArabicName,
            transliteratedName = firstRow.surahTransliteratedName,
            isMakki = firstRow.surahIsMakki,
            verseCount = firstRow.surahVerseCount,
            startPage = firstRow.surahStartPage,
            pinOrder = firstRow.surahPinOrder
        )

        val ayahTranslations = rawDetails.mapNotNull { raw ->
            if (raw.ayahTranslationId != null &&
                raw.ayahTranslationTranslationId != null &&
                raw.ayahTranslationText != null &&
                raw.ayahTranslationAyahId != null &&
                raw.translationId != null &&
                raw.translationName != null &&
                raw.translationLanguageId != null &&
                raw.translationKey != null &&
                raw.translationSelected != null) {

                val ayahTranslation = AyahTranslation(
                    id = raw.ayahTranslationId,
                    translationId = raw.ayahTranslationTranslationId,
                    text = raw.ayahTranslationText,
                    ayahId = raw.ayahTranslationAyahId
                )

                val translation = Translation(
                    id = raw.translationId,
                    name = raw.translationName,
                    languageId = raw.translationLanguageId,
                    key = raw.translationKey,
                    selected = raw.translationSelected,
                    order = raw.translationOrder
                )

                AyahTranslationWithFootnotes(ayahTranslation, translation)
            } else {
                null
            }
        }.sortedBy { it.translation.order }

        FullAyahDetails(ayah = ayah, surah = surah, ayahTranslations = ayahTranslations)
    }.sortedWith(compareBy({ it.surah.id }, { it.ayah.number }))
}
