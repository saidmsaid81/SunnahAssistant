package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT count(*) FROM surahs")
    suspend fun countSurah(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(surah: Surah)

    @Query("SELECT * FROM surahs WHERE id = :id ")
    suspend fun getSurahById(id: Int): Surah

    @Query("SELECT * FROM surahs ORDER BY CASE WHEN pin_order IS NULL THEN 1 ELSE 0 END, pin_order ASC, id ASC LIMIT 3")
    fun getFirst3Surahs(): Flow<List<Surah>>

    @Query("SELECT * FROM surahs ORDER BY CASE WHEN pin_order IS NULL THEN 1 ELSE 0 END, pin_order ASC, id ASC")
    fun getAllSurahs(): PagingSource<Int, Surah>

    @Query("SELECT * FROM surahs WHERE start_page BETWEEN 1 AND :page ORDER BY start_page DESC LIMIT 1 ")
    suspend fun getSurahByPage(page: Int): Surah?

    @Query("UPDATE surahs SET pin_order = :pinOrder WHERE id = :surahId")
    suspend fun updatePinOrder(surahId: Int, pinOrder: Int?)

    @Query("SELECT COUNT(*) FROM surahs WHERE pin_order IS NOT NULL")
    suspend fun getPinnedSurahCount(): Int

    @Query("SELECT * FROM surahs WHERE pin_order IS NOT NULL ORDER BY pin_order ASC")
    fun getPinnedSurahs(): Flow<List<Surah>>

    @Query("SELECT MAX(pin_order) FROM surahs WHERE pin_order IS NOT NULL")
    suspend fun getMaxPinOrder(): Int?
}