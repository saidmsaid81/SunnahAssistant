package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ResourcesRepository =
        ResourcesRepository.getInstance(application)

    private val _lastReadSurah = MutableStateFlow<Surah?>(null)
    val lastReadSurah: StateFlow<Surah?> = _lastReadSurah

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateQuranData()
        }
    }

    fun getFirst5Surahs(): Flow<List<Surah>> {
        return repository.getFirst5Surahs()
    }

    fun resourceItems() = repository.resourceItems()

    fun setLatReadPage(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = repository.getSurahByPage(page)
            surah?.let {
                val modifiedSurah = it.copy(startPage = page)
                _lastReadSurah.emit(modifiedSurah)
            }
        }
    }
}