package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PageTranslationViewModel(application: Application) : AyahTranslationViewModel(application) {
    private val quranTranslationRepository = QuranTranslationRepository.getInstance(getApplication())
    private val _ayahDetails = MutableStateFlow<MutableMap<Int, List<FullAyahDetails>>>(mutableMapOf())
    val ayahDetails: StateFlow<Map<Int, List<FullAyahDetails>>> = _ayahDetails

    fun updateAyahDetailsFromPage(selectedPageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _ayahDetails.update { currentMap ->
                currentMap.apply {
                    put(selectedPageNumber, quranTranslationRepository.getFullAyahDetailsByPageNumber(selectedPageNumber))
                }
            }
        }
    }
}