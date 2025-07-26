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

    @Query("SELECT * FROM surahs ORDER BY id ASC LIMIT 5")
    fun getFirst5Surahs(): Flow<List<Surah>>

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun getAllSurahs(): PagingSource<Int, Surah>

    @Query("SELECT * FROM surahs WHERE start_page BETWEEN 1 AND :page ORDER BY start_page DESC LIMIT 1 ")
    suspend fun getSurahByPage(page: Int): Surah


}