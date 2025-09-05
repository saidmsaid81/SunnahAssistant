package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.model.SurahWithPin
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT count(*) FROM surahs")
    suspend fun countSurah(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(surah: Surah)


    @Transaction
    @Query("SELECT * FROM surahs ORDER BY CASE WHEN (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) IS NULL THEN 1 ELSE 0 END, (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) ASC, id ASC LIMIT 3")
    fun getFirst3SurahsWithPin(): Flow<List<SurahWithPin>>

    @Transaction
    @Query("SELECT * FROM surahs ORDER BY CASE WHEN (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) IS NULL THEN 1 ELSE 0 END, (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) ASC, id ASC")
    fun getAllSurahsWithPin(): PagingSource<Int, SurahWithPin>

    @Query("SELECT * FROM surahs WHERE start_page BETWEEN 1 AND :page ORDER BY start_page DESC LIMIT 1 ")
    suspend fun getSurahByPage(page: Int): Surah?

    @Transaction
    @Query("SELECT * FROM surahs WHERE arabic_name LIKE :query OR transliterated_name LIKE :query ORDER BY CASE WHEN (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) IS NULL THEN 1 ELSE 0 END, (SELECT pin_order FROM pinned_surahs WHERE surah_id = surahs.id) ASC, id ASC")
    fun searchSurahsWithPin(query: String): PagingSource<Int, SurahWithPin>
}