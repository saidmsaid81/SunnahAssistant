package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import kotlinx.coroutines.flow.Flow

class SurahListViewModel(application: Application) : AndroidViewModel(application) {
    private val mQuranRepository =
        QuranRepository.getInstance(application)

    fun getAllSurahs(): Flow<PagingData<Surah>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                mQuranRepository.getAllSurahs()
            }
        ).flow.cachedIn(viewModelScope)
    }
}