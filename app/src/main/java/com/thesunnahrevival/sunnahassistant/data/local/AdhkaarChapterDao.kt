package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarChapter
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkaarChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adhkaarChapter: AdhkaarChapter)

    @Query("SELECT COUNT(*) FROM adhkaar_chapters")
    suspend fun countAdhkaarChapters(): Int

    @Query("SELECT * FROM adhkaar_chapters WHERE language = :language ORDER BY chapter_id LIMIT 3")
    fun getFirstThreeChapters(language: String): Flow<List<AdhkaarChapter>>

    @Query("SELECT * FROM adhkaar_chapters WHERE language = :language ORDER BY chapter_id")
    fun getAllChaptersPagingSource(language: String): PagingSource<Int, AdhkaarChapter>
}