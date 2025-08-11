package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.sunnahassistant.data.model.*

@Dao
interface AyahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayah: Ayah)

    @Query("SELECT " +
            "a.id AS ayah_id, " +
            "a.number AS ayah_number, " +
            "a.surah_id AS ayah_surah_id, " +
            "a.arabic_text AS ayah_arabic_text, " +
            "a.bookmarked AS ayah_bookmarked, " +
            "at.id AS ayah_translation_id, " +
            "at.translation_id AS ayah_translation_translation_id, " +
            "at.text AS ayah_translation_text, " +
            "at.ayah_id AS ayah_translation_ayah_id, " +
            "t.id AS translation_id, " +
            "t.name AS translation_name, " +
            "t.language_id AS translation_language_id, " +
            "t.`key` AS translation_key, " +
            "t.selected AS translation_selected, " +
            "t.`order` AS translation_order, " +
            "s.id AS surah_id, " +
            "s.arabic_name AS surah_arabic_name," +
            "s.transliterated_name AS surah_transliterated_name," +
            "s.is_makki AS surah_is_makki," +
            "s.verse_count AS surah_verse_count," +
            "s.start_page AS surah_start_page," +
            "s.pin_order AS surah_pin_order " +
            "FROM ayahs a " +
            "LEFT JOIN ayah_translations at ON at.ayah_id = a.id " +
            "LEFT JOIN translations t ON t.id = at.translation_id " +
            "JOIN surahs s ON s.id = a.surah_id " +
            "WHERE a.id = :ayahId AND t.selected = 1")
    suspend fun getFullAyahDetailsById(ayahId: Int): List<FullAyahDetailsRaw>

    @Query(
        "SELECT " +
                "a.id AS ayah_id, " +
                "a.number AS ayah_number, " +
                "a.surah_id AS ayah_surah_id, " +
                "a.arabic_text AS ayah_arabic_text, " +
                "a.bookmarked AS ayah_bookmarked, " +
                "at.id AS ayah_translation_id, " +
                "at.translation_id AS ayah_translation_translation_id, " +
                "at.text AS ayah_translation_text, " +
                "at.ayah_id AS ayah_translation_ayah_id, " +
                "t.id AS translation_id, " +
                "t.name AS translation_name, " +
                "t.language_id AS translation_language_id, " +
                "t.`key` AS translation_key, " +
                "t.selected AS translation_selected, " +
                "t.`order` AS translation_order, " +
                "s.id AS surah_id, " +
                "s.arabic_name AS surah_arabic_name," +
                "s.transliterated_name AS surah_transliterated_name," +
                "s.is_makki AS surah_is_makki," +
                "s.verse_count AS surah_verse_count," +
                "s.start_page AS surah_start_page," +
                "s.pin_order AS surah_pin_order " +
                "FROM ayahs a " +
                "LEFT JOIN ayah_translations at ON at.ayah_id = a.id " +
                "LEFT JOIN translations t ON t.id = at.translation_id " +
                "JOIN surahs s ON s.id = a.surah_id " +
                "WHERE a.id IN (SELECT l.ayah_id FROM lines l WHERE l.page_number = :pageNumber) AND t.selected = 1 "
    )
    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int): List<FullAyahDetailsRaw>

    @Update
    suspend fun updateAyah(ayah: Ayah)

    @Query("UPDATE ayahs SET bookmarked = :bookmarked WHERE id = :ayahId")
    suspend fun updateAyahBookmarkStatus(ayahId: Int, bookmarked: Boolean)

    @Query("SELECT * FROM ayahs WHERE bookmarked = 1 ORDER BY surah_id, number")
    fun getBookmarkedAyahs(): PagingSource<Int, AyahWithSurah>
}

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
) {
    fun toFullAyahDetails(): FullAyahDetails {
        val ayah = Ayah(
            id = ayahId,
            number = ayahNumber,
            surahId = ayahSurahId,
            arabicText = ayahArabicText,
            bookmarked = ayahBookmarked
        )

        val surah = Surah(
            id = surahId,
            arabicName = surahArabicName,
            transliteratedName = surahTransliteratedName,
            isMakki = surahIsMakki,
            verseCount = surahVerseCount,
            startPage = surahStartPage,
            pinOrder = surahPinOrder
        )

        val ayahTranslations = if (ayahTranslationId != null && 
                                   ayahTranslationTranslationId != null && 
                                   ayahTranslationText != null && 
                                   ayahTranslationAyahId != null &&
                                   translationId != null &&
                                   translationName != null &&
                                   translationLanguageId != null &&
                                   translationKey != null &&
                                   translationSelected != null) {
            val ayahTranslation = AyahTranslation(
                id = ayahTranslationId,
                translationId = ayahTranslationTranslationId,
                text = ayahTranslationText,
                ayahId = ayahTranslationAyahId
            )

            val translation = Translation(
                id = translationId,
                name = translationName,
                languageId = translationLanguageId,
                key = translationKey,
                selected = translationSelected,
                order = translationOrder
            )

            listOf(AyahTranslationWithFootnotes(ayahTranslation, translation))
        } else {
            emptyList()
        }

        return FullAyahDetails(ayah = ayah, surah = surah, ayahTranslations = ayahTranslations)
    }
}

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
