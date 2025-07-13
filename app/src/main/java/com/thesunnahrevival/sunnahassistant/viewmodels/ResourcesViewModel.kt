package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ResourcesRepository =
        ResourcesRepository.getInstance(application)

    init {
        viewModelScope.launch {
            repository.prepopulateQuranData()
        }
    }

    fun getFirst5Surahs(): Flow<List<Surah>> {
        return repository.getFirst5Surahs()
    }

    fun resourceItems() = repository.resourceItems()

}