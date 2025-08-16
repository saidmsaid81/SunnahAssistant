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

    @Query("SELECT * FROM adhkaar_chapters WHERE language = :language ORDER BY CASE WHEN pin_order IS NULL THEN 1 ELSE 0 END, pin_order ASC, chapter_id ASC LIMIT 3")
    fun getFirstThreeChapters(language: String): Flow<List<AdhkaarChapter>>

    @Query("SELECT * FROM adhkaar_chapters WHERE language = :language ORDER BY CASE WHEN pin_order IS NULL THEN 1 ELSE 0 END, pin_order ASC, chapter_id ASC")
    fun getAllChaptersPagingSource(language: String): PagingSource<Int, AdhkaarChapter>

    @Query("SELECT chapter_name FROM adhkaar_chapters WHERE chapter_id = :id AND language = :language")
    suspend fun getChapterNameByChapterId(id: Int, language: String): String?

    @Query("SELECT * FROM adhkaar_chapters WHERE chapter_id = :chapterId")
    suspend fun getChapterById(chapterId: Int): AdhkaarChapter

    @Query("UPDATE adhkaar_chapters SET pin_order = :pinOrder WHERE chapter_id = :chapterId")
    suspend fun updatePinOrder(chapterId: Int, pinOrder: Int?)

    @Query("SELECT distinct(chapter_id) FROM adhkaar_chapters WHERE pin_order IS NOT NULL")
    suspend fun getPinnedChapterCount(): List<Int>


    @Query("SELECT MAX(pin_order) FROM adhkaar_chapters WHERE pin_order IS NOT NULL")
    suspend fun getMaxPinOrder(): Int?
}