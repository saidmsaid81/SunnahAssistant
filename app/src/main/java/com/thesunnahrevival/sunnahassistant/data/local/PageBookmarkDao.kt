package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.sunnahassistant.data.model.PageBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface PageBookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageBookmark(pageBookmark: PageBookmark)

    @Delete
    suspend fun deletePageBookmark(pageBookmark: PageBookmark)

    @Query("DELETE FROM page_bookmarks WHERE page_number = :pageNumber")
    suspend fun deletePageBookmarkByPageNumber(pageNumber: Int)

    @Query("SELECT * FROM page_bookmarks WHERE page_number = :pageNumber")
    suspend fun getPageBookmarkByPageNumber(pageNumber: Int): PageBookmark?

    @Query("SELECT * FROM page_bookmarks ORDER BY page_number ASC")
    fun getAllPageBookmarks(): PagingSource<Int, PageBookmark>

    @Query("SELECT EXISTS(SELECT 1 FROM page_bookmarks WHERE page_number = :pageNumber)")
    suspend fun isPageBookmarked(pageNumber: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM page_bookmarks WHERE page_number = :pageNumber)")
    fun isPageBookmarkedFlow(pageNumber: Int): Flow<Boolean>

    @Query("SELECT pb.* FROM page_bookmarks pb JOIN surahs s ON pb.page_number >= s.start_page WHERE s.arabic_name LIKE :query OR s.transliterated_name LIKE :query ORDER BY pb.page_number ASC")
    fun searchPageBookmarks(query: String): PagingSource<Int, PageBookmark>
}