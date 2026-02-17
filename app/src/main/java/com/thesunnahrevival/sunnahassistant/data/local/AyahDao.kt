package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.dto.FullAyahDetailsRaw
import com.thesunnahrevival.sunnahassistant.data.model.embedded.AyahWithSurahEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.Ayah

@Dao
interface AyahDao {
    @Query("SELECT count(*) FROM ayahs")
    suspend fun countAyahs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayah: Ayah)

    @Query("SELECT " +
            "a.id AS ayah_id, " +
            "a.number AS ayah_number, " +
            "a.surah_id AS ayah_surah_id, " +
            "a.arabic_text AS ayah_arabic_text, " +
            "ab.id IS NOT NULL AS ayah_bookmarked, " +
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
            "p.pin_order AS surah_pin_order " +
            "FROM ayahs a " +
            "LEFT JOIN ayah_translations at ON at.ayah_id = a.id AND at.translation_id IN (SELECT id FROM translations WHERE selected = 1) " +
            "LEFT JOIN translations t ON t.id = at.translation_id " +
            "LEFT JOIN ayah_bookmarks ab ON ab.ayah_id = a.id " +
            "JOIN surahs s ON s.id = a.surah_id " +
            "LEFT JOIN pinned_surahs p ON p.surah_id = s.id " +
            "WHERE a.id = :ayahId")
    suspend fun getFullAyahDetailsById(ayahId: Int): List<FullAyahDetailsRaw>

    @Query(
        "SELECT " +
                "a.id AS ayah_id, " +
                "a.number AS ayah_number, " +
                "a.surah_id AS ayah_surah_id, " +
                "a.arabic_text AS ayah_arabic_text, " +
                "ab.id IS NOT NULL AS ayah_bookmarked, " +
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
                "p.pin_order AS surah_pin_order " +
                "FROM ayahs a " +
                "LEFT JOIN ayah_translations at ON at.ayah_id = a.id AND at.translation_id IN (SELECT id FROM translations WHERE selected = 1) " +
                "LEFT JOIN translations t ON t.id = at.translation_id " +
                "LEFT JOIN ayah_bookmarks ab ON ab.ayah_id = a.id " +
                "JOIN surahs s ON s.id = a.surah_id " +
                "LEFT JOIN pinned_surahs p ON p.surah_id = s.id " +
                "WHERE a.id IN (SELECT l.ayah_id FROM lines l WHERE l.page_number = :pageNumber) ORDER BY t.`order` ASC"
    )
    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int): List<FullAyahDetailsRaw>


    @Query("SELECT * FROM ayahs " +
            "JOIN ayah_bookmarks ab ON ab.ayah_id = ayahs.id " +
            "ORDER BY surah_id, number")
    fun getBookmarkedAyahs(): PagingSource<Int, AyahWithSurahEmbedded>

    @Query("SELECT page_number FROM lines WHERE ayah_id = :ayahId LIMIT 1")
    suspend fun getPageNumberByAyahId(ayahId: Int): Int?

    @Query("SELECT * FROM ayahs a " +
            "JOIN ayah_bookmarks ab ON ab.ayah_id = a.id " +
            "JOIN surahs s ON a.surah_id = s.id " +
            "WHERE (s.arabic_name LIKE :query OR s.transliterated_name LIKE :query OR a.arabic_text LIKE :query) ORDER BY a.surah_id, a.number")
    fun searchBookmarkedAyahs(query: String): PagingSource<Int, AyahWithSurahEmbedded>
}
