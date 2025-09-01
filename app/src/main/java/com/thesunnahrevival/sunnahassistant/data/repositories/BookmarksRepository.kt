package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.PageBookmarkDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao
import com.thesunnahrevival.sunnahassistant.data.model.PageBookmark
import com.thesunnahrevival.sunnahassistant.data.model.PageBookmarkWithSurah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarksRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: BookmarksRepository? = null

        fun getInstance(context: Context): BookmarksRepository {
            return instance ?: synchronized(this) {
                instance ?: BookmarksRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val pageBookmarkDao: PageBookmarkDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).pageBookmarkDao()

    private val surahDao: SurahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).surahDao()

    fun getBookmarkedAyahs() = ayahDao.getBookmarkedAyahs()

    fun searchBookmarkedAyahs(query: String) = ayahDao.searchBookmarkedAyahs(query)


    fun getBookmarkedPagesWithSurah(): Flow<PagingData<PageBookmarkWithSurah>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                pageBookmarkDao.getAllPageBookmarks()
            }
        ).flow.map { pagingData ->
            pagingData.map { pageBookmark ->
                val surah = surahDao.getSurahByPage(pageBookmark.pageNumber) 
                    ?: throw IllegalStateException("No Surah found for page ${pageBookmark.pageNumber}")
                PageBookmarkWithSurah(pageBookmark, surah)
            }
        }
    }

    fun searchBookmarkedPagesWithSurah(query: String): Flow<PagingData<PageBookmarkWithSurah>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                pageBookmarkDao.searchPageBookmarks(query)
            }
        ).flow.map { pagingData ->
            pagingData.map { pageBookmark ->
                val surah = surahDao.getSurahByPage(pageBookmark.pageNumber) 
                    ?: throw IllegalStateException("No Surah found for page ${pageBookmark.pageNumber}")
                PageBookmarkWithSurah(pageBookmark, surah)
            }
        }
    }

    suspend fun isPageBookmarked(pageNumber: Int) = pageBookmarkDao.isPageBookmarked(pageNumber)

    suspend fun togglePageBookmark(pageNumber: Int) {
        val existingBookmark = pageBookmarkDao.getPageBookmarkByPageNumber(pageNumber)
        if (existingBookmark != null) {
            pageBookmarkDao.deletePageBookmark(existingBookmark)
        } else {
            pageBookmarkDao.insertPageBookmark(PageBookmark(pageNumber = pageNumber))
        }
    }

    suspend fun getPageNumberByAyahId(ayahId: Int): Int? = ayahDao.getPageNumberByAyahId(ayahId)

}