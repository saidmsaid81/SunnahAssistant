package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.embedded.AdhkaarChapterWithPinEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarChapter
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkaarChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adhkaarChapter: AdhkaarChapter)

    @Query("SELECT COUNT(*) FROM adhkaar_chapters")
    suspend fun countAdhkaarChapters(): Int

    @Query("""
        SELECT *
        FROM adhkaar_chapters ac 
        LEFT JOIN pinned_adhkaar_chapters pac ON ac.chapter_id = pac.chapter_id 
        WHERE ac.language = :language 
        ORDER BY CASE WHEN pac.pin_order IS NULL THEN 1 ELSE 0 END, pac.pin_order ASC, ac.chapter_id ASC 
        LIMIT 3
    """)
    fun getFirstThreeChapters(language: String): Flow<List<AdhkaarChapterWithPinEmbedded>>

    @Query("""
        SELECT *
        FROM adhkaar_chapters ac 
        LEFT JOIN pinned_adhkaar_chapters pac ON ac.chapter_id = pac.chapter_id 
        WHERE ac.language = :language 
        ORDER BY CASE WHEN pac.pin_order IS NULL THEN 1 ELSE 0 END, pac.pin_order ASC, ac.chapter_id ASC
    """)
    fun getAllChaptersPagingSource(language: String): PagingSource<Int, AdhkaarChapterWithPinEmbedded>

    @Query("""
        SELECT * 
        FROM adhkaar_chapters ac 
        LEFT JOIN pinned_adhkaar_chapters pac ON ac.chapter_id = pac.chapter_id 
        WHERE ac.language = :language AND (ac.chapter_name LIKE :query OR ac.category_name LIKE :query) 
        ORDER BY CASE WHEN pac.pin_order IS NULL THEN 1 ELSE 0 END, pac.pin_order ASC, ac.chapter_id ASC
    """)
    fun getChaptersByQuery(language: String, query: String): PagingSource<Int, AdhkaarChapterWithPinEmbedded>

    @Query("SELECT chapter_name FROM adhkaar_chapters WHERE chapter_id = :id AND language = :language")
    suspend fun getChapterNameByChapterId(id: Int, language: String): String?

    @Query("SELECT * FROM adhkaar_chapters WHERE chapter_id = :chapterId")
    suspend fun getChapterById(chapterId: Int): AdhkaarChapter
}