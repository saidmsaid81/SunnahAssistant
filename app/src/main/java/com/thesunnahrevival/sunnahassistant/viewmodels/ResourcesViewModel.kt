package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.ResourceItem
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : SurahListViewModel(application) {
    private val repository: ResourcesRepository =
        ResourcesRepository.getInstance(application)

    private val surahRepository: SurahRepository =
        SurahRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ResourcesUIState())
    val uiState: StateFlow<ResourcesUIState> = _uiState.asStateFlow()

    private val _lastReadSurah = MutableStateFlow<Surah?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                repository.prepopulateQuranData()
                
                combine(
                    surahRepository.getFirst3Surahs(),
                    _lastReadSurah
                ) { surahs, lastReadSurah ->
                    ResourcesUIState(
                        isDataReady = true,
                        isLoading = false,
                        surahs = surahs,
                        lastReadSurah = lastReadSurah,
                        resourceItems = repository.resourceItems(),
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setLastReadPage(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val surah = surahRepository.getSurahByPage(page)
                surah?.let {
                    val modifiedSurah = it.copy(startPage = page)
                    _lastReadSurah.emit(modifiedSurah)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class ResourcesUIState(
    val isDataReady: Boolean = false,
    val isLoading: Boolean = true,
    val surahs: List<Surah> = emptyList(),
    val lastReadSurah: Surah? = null,
    val resourceItems: List<ResourceItem> = emptyList(),
    val error: String? = null
)