package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.flow.Flow

class AdhkaarChapterListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ResourcesRepository.getInstance(application)

    var firstVisiblePosition = 0

    fun getAllAdhkaarChapters(): Flow<PagingData<AdhkaarChapter>> {
        val locale = getApplication<Application>().getLocale()

        val language = if (locale.language.startsWith("ar")) "ar" else "en"
        return Pager(
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 40,
                enablePlaceholders = true,
                initialLoadSize = firstVisiblePosition + 20
            ),
            pagingSourceFactory = {
                repository.getAllAdhkaarChapters(language)
            }
        ).flow.cachedIn(viewModelScope)
    }
}