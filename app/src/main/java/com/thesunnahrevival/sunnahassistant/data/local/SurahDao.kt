package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.Surah

@Dao
interface SurahDao {
    @Query("SELECT count(*) FROM surahs")
    suspend fun countSurah(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(surah: Surah)

    @Query("SELECT * FROM surahs WHERE id = :id ")
    suspend fun getSurahById(id: Int): Surah


}