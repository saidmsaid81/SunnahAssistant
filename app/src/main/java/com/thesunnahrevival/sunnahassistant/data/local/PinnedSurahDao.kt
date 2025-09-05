package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.PinnedSurah

@Dao
interface PinnedSurahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pinnedSurah: PinnedSurah)

    @Query("DELETE FROM pinned_surahs WHERE surah_id = :surahId")
    suspend fun deleteBysurahId(surahId: Int)

    @Query("SELECT COUNT(*) FROM pinned_surahs")
    suspend fun getPinnedCount(): Int

    @Query("SELECT MAX(pin_order) FROM pinned_surahs")
    suspend fun getMaxPinOrder(): Int?

    @Query("SELECT * FROM pinned_surahs WHERE surah_id = :surahId")
    suspend fun getPinnedSurahById(surahId: Int): PinnedSurah?
}