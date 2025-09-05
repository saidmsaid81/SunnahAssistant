package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedAdhkaarChapter

@Dao
interface PinnedAdhkaarChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinnedChapter(pinnedChapter: PinnedAdhkaarChapter)

    @Query("DELETE FROM pinned_adhkaar_chapters WHERE chapter_id = :chapterId")
    suspend fun deletePinnedChapterById(chapterId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_adhkaar_chapters WHERE chapter_id = :chapterId)")
    suspend fun isChapterPinned(chapterId: Int): Boolean

    @Query("SELECT COALESCE(MAX(pin_order), 0) FROM pinned_adhkaar_chapters")
    suspend fun getMaxPinOrder(): Int

    @Query("SELECT COUNT(*) FROM pinned_adhkaar_chapters")
    suspend fun getPinnedChaptersCount(): Int
}
