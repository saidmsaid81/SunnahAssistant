package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PageTranslationViewModel(application: Application) : AyahTranslationViewModel(application) {
    private val quranTranslationRepository = QuranTranslationRepository.getInstance(getApplication())
    private val _ayahDetails = MutableStateFlow<List<FullAyahDetails>>(emptyList())
    val ayahDetails: StateFlow<List<FullAyahDetails>> = _ayahDetails.asStateFlow()

    private val selectedPageNumber = MutableSharedFlow<Int>(replay = 1)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            selectedPageNumber.collect { pageNumber ->
                _ayahDetails.update { quranTranslationRepository.getFullAyahDetailsByPageNumber(pageNumber) }
            }
        }
    }

    fun setSelectedPage(selectedPageNumber: Int) {
        viewModelScope.launch {
            this@PageTranslationViewModel.selectedPageNumber.emit(selectedPageNumber)
        }
    }

    fun nextPage() {
        viewModelScope.launch {
            selectedPageNumber.replayCache.firstOrNull()?.let { pageNumber ->
                if (pageNumber < 604) {
                    selectedPageNumber.emit(pageNumber + 1)
                }
            }
        }
    }

    fun previousPage() {
        viewModelScope.launch {
            selectedPageNumber.replayCache.firstOrNull()?.let { pageNumber ->
                if (pageNumber > 1) {
                    selectedPageNumber.emit(pageNumber - 1)
                }
            }
        }
    }
}