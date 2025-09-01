package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.*
import com.thesunnahrevival.sunnahassistant.data.model.AyahBookmark

@Dao
interface AyahBookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayahBookmark: AyahBookmark)

    @Delete
    suspend fun delete(ayahBookmark: AyahBookmark)

    @Query("SELECT * FROM ayah_bookmarks WHERE ayah_id = :ayahId ")
    suspend fun getAyahBookmark(ayahId: Int): AyahBookmark?
}