package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.dto.PageBookmarkWithSurah
import com.thesunnahrevival.sunnahassistant.data.model.embedded.AyahWithSurahEmbedded
import com.thesunnahrevival.sunnahassistant.data.repositories.BookmarksRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val bookmarksRepository = BookmarksRepository.getInstance(application)

    var firstVisiblePosition = 0

    private val searchQuery = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val bookmarkedAyahsFlow: Flow<PagingData<AyahWithSurahEmbedded>> = searchQuery
        .map { it?.trim().orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            Pager(
                PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 40,
                    enablePlaceholders = true,
                    initialLoadSize = firstVisiblePosition + 20
                ),
                pagingSourceFactory = {
                    if (query.isBlank()) {
                        bookmarksRepository.getBookmarkedAyahs()
                    } else {
                        val q = "%$query%"
                        bookmarksRepository.searchBookmarkedAyahs(q)
                    }
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class)
    val bookmarkedPagesFlow: Flow<PagingData<PageBookmarkWithSurah>> = searchQuery
        .map { it?.trim().orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                bookmarksRepository.getBookmarkedPagesWithSurah()
            } else {
                val q = "%$query%"
                bookmarksRepository.searchBookmarkedPagesWithSurah(q)
            }
        }
        .cachedIn(viewModelScope)

    suspend fun getPageNumberByAyahId(ayahId: Int): Int? = bookmarksRepository.getPageNumberByAyahId(ayahId)

    fun setSearchQuery(query: String?) {
        if (firstVisiblePosition != 0) {
            firstVisiblePosition = 0
        }
        searchQuery.value = query
    }

}