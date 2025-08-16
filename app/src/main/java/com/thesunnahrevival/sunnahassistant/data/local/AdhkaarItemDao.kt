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
}