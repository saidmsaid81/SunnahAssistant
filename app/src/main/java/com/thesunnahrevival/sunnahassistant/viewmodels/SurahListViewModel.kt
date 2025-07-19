package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import kotlinx.coroutines.flow.Flow

class SurahListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository =
        SurahRepository.getInstance(application)

    var firstVisiblePosition = 0
    fun getAllSurahs(): Flow<PagingData<Surah>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = firstVisiblePosition + 20
            ),
            pagingSourceFactory = {
                repository.getAllSurahs()
            }
        ).flow.cachedIn(viewModelScope)
    }
}