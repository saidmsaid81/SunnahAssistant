package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.SurahWithPin
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

open class SurahListViewModel(application: Application) : AndroidViewModel(application), SurahPinnable {
    private val repository =
        SurahRepository.getInstance(application)

    private val pinHelper = PinHelper(this, surahRepository = repository)

    var firstVisiblePosition = 0

    private val searchQuery = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val surahsFlow: Flow<PagingData<SurahWithPin>> = searchQuery
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
                        repository.getAllSurahs()
                    } else {
                        val q = "%$query%"
                        repository.searchSurahs(q)
                    }
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setSearchQuery(query: String?) {
        if (firstVisiblePosition != 0) {
            firstVisiblePosition = 0
        }
        searchQuery.value = query
    }

    override fun toggleSurahPin(surahId: Int, onResult: (SurahRepository.PinResult) -> Unit) {
        pinHelper.toggleSurahPin(surahId, onResult)
    }
}