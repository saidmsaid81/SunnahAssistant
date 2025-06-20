package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import kotlinx.coroutines.flow.Flow

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: QuranRepository =
        QuranRepository.getInstance(application)

    fun getFirst5Surahs(): Flow<List<Surah>> {
        return mRepository.getFirst5Surahs()
    }
}