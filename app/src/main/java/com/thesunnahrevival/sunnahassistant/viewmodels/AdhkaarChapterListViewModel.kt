package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarChapterRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class AdhkaarChapterListViewModel(application: Application) : AndroidViewModel(application), AdhkaarChapterPinnable {
    private val repository = ResourcesRepository.getInstance(application)
    private val adhkaarChapterRepository = AdhkaarChapterRepository.getInstance(application)
    private val pinHelper = PinHelper(this, adhkaarChapterRepository)

    var firstVisiblePosition = 0

    private val searchQuery = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val chaptersFlow: Flow<PagingData<AdhkaarChapter>> = searchQuery
        .map { it?.trim().orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val locale = getApplication<Application>().getLocale()
            val language = if (locale.language.startsWith("ar")) "ar" else "en"
            Pager(
                PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 40,
                    enablePlaceholders = true,
                    initialLoadSize = firstVisiblePosition + 20
                ),
                pagingSourceFactory = {
                    if (query.isBlank()) {
                        adhkaarChapterRepository.getAllChapters(language)
                    } else {
                        val q = "%$query%"
                        adhkaarChapterRepository.searchChapters(language, q)
                    }
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setSearchQuery(query: String?) {
        if (firstVisiblePosition != 0) firstVisiblePosition = 0
        searchQuery.value = query
    }

    override fun toggleChapterPin(chapterId: Int, onResult: (AdhkaarChapterRepository.PinResult) -> Unit) {
        pinHelper.toggleChapterPin(chapterId, onResult)
    }
}