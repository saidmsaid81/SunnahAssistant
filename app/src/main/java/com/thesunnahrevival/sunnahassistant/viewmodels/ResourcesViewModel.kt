package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: QuranRepository =
        QuranRepository.getInstance(application)

    init {
        viewModelScope.launch {
            mRepository.prepopulateQuranData()
        }
    }

    fun getFirst5Surahs(): Flow<List<Surah>> {
        return mRepository.getFirst5Surahs()
    }
}