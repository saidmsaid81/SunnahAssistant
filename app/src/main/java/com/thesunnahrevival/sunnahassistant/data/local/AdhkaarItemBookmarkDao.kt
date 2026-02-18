package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.embedded.BookmarkedAdhkaarDataEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItemBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkaarItemBookmarkDao {

    @Insert
    suspend fun insert(adhkaarItemBookmark: AdhkaarItemBookmark)

    @Delete
    suspend fun delete(adhkaarItemBookmark: AdhkaarItemBookmark)

    @Query("SELECT * FROM adhkaar_item_bookmarks WHERE adhkaar_item_id = :adhkaarItemId")
    suspend fun getAdhkaarItemBookmark(adhkaarItemId: Int): AdhkaarItemBookmark?

    @Query("""
        SELECT ai.chapter_id as chapterId, ac.chapter_name as chapterName, 
               ai.item_id as itemId, ai.item_translation as itemTranslation
        FROM adhkaar_items ai
        JOIN adhkaar_chapters ac ON ai.chapter_id = ac.chapter_id 
        JOIN adhkaar_item_bookmarks ab ON ai.item_id = ab.adhkaar_item_id
        WHERE ai.language = :language AND ac.language = :language
        ORDER BY ai.chapter_id, ai.item_id
    """)
    fun getBookmarkedAdhkaarData(language: String): Flow<List<BookmarkedAdhkaarDataEmbedded>>

    @Query("""
        SELECT ai.chapter_id as chapterId, ac.chapter_name as chapterName, 
               ai.item_id as itemId, ai.item_translation as itemTranslation
        FROM adhkaar_items ai
        JOIN adhkaar_chapters ac ON ai.chapter_id = ac.chapter_id 
        JOIN adhkaar_item_bookmarks ab ON ai.item_id = ab.adhkaar_item_id
        WHERE ai.language = :language AND ac.language = :language
        AND (ac.chapter_name LIKE :query OR ai.item_translation LIKE :query OR ai.item_translation LIKE :query)
        ORDER BY ai.chapter_id, ai.item_id
    """)
    fun searchBookmarkedAdhkaarData(language: String, query: String): Flow<List<BookmarkedAdhkaarDataEmbedded>>
}