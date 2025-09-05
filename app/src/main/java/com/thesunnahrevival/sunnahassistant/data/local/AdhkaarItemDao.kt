package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.embedded.AdhkaarItemWithBookmarkEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkaarItemDao {
    @Query("SELECT a.*, " +
            "CASE WHEN aib.id IS NOT NULL THEN 1 ELSE 0 END AS bookmarked " +
            "FROM adhkaar_items a " +
            "LEFT JOIN adhkaar_item_bookmarks aib ON aib.adhkaar_item_id = a.item_id " +
            "WHERE a.chapter_id = :chapterId")
    fun getAdhkaarItemsByChapterId(chapterId: Int): Flow<List<AdhkaarItemWithBookmarkEmbedded>>

    @Query("SELECT EXISTS(SELECT 1 FROM adhkaar_items)")
    suspend fun doesAdhkaarItemsExist(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(adhkaarItems: List<AdhkaarItem>)
}