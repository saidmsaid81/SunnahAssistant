package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.AyahWithSurah
import com.thesunnahrevival.sunnahassistant.data.repositories.BookmarksRepository
import kotlinx.coroutines.flow.Flow

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val bookmarksRepository = BookmarksRepository.getInstance(application)

    var firstVisiblePosition = 0


    fun getBookmarkedAyahs(): Flow<PagingData<AyahWithSurah>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = firstVisiblePosition + 20
            ),
            pagingSourceFactory = {
                bookmarksRepository.getBookmarkedAyahs()
            }
        ).flow.cachedIn(viewModelScope)
    }
}