package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkaarItemDao {
    @Query("SELECT * FROM adhkaar_items WHERE chapter_id = :chapterId")
    fun getAdhkaarItemsByChapterId(chapterId: Int): Flow<List<AdhkaarItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM adhkaar_items)")
    suspend fun doesAdhkaarItemsExist(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(adhkaarItems: List<AdhkaarItem>)

    @Query("UPDATE adhkaar_items SET bookmarked = :bookmarked WHERE item_id = :itemId")
    suspend fun updateBookmarkStatus(itemId: Int, bookmarked: Boolean)

    @Query("""
        SELECT ai.chapter_id as chapterId, ac.chapter_name as chapterName, 
               ai.item_id as itemId, ai.item_translation as itemTranslation
        FROM adhkaar_items ai
        INNER JOIN adhkaar_chapters ac ON ai.chapter_id = ac.chapter_id 
        WHERE ai.bookmarked = 1 AND ai.language = :language AND ac.language = :language
        ORDER BY ai.chapter_id, ai.item_id
    """)
    fun getBookmarkedAdhkaarData(language: String): Flow<List<com.thesunnahrevival.sunnahassistant.data.model.BookmarkedAdhkaarData>>
}