package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val mQuranRepository: QuranRepository =
        QuranRepository.getInstance(application)

    val translationUiState = mQuranRepository.getTranslations().map { translations ->
        TranslationUiState(
            allTranslations = translations,
            selectedTranslations = translations.filter { it.selected }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TranslationUiState()
    )

    fun toggleTranslationSelection(translation: Translation) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTranslation = translation.copy(selected = !translation.selected)
            mQuranRepository.updateTranslation(updatedTranslation)
        }
    }

    data class TranslationUiState(
        val allTranslations: List<Translation> = listOf(),
        val selectedTranslations: List<Translation> = listOf()
    )
}