package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val quranTranslationRepository =
        QuranTranslationRepository.getInstance(application)

    val translationUiState = quranTranslationRepository.getTranslations().map { translations ->
        TranslationUiState(
            allTranslations = translations,
            selectedTranslations = translations.filter { it.selected }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TranslationUiState()
    )

    fun toggleTranslationSelection(translation: Translation, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTranslation = translation.copy(selected = !translation.selected)
            quranTranslationRepository.updateTranslation(updatedTranslation)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    data class TranslationUiState(
        val allTranslations: List<Translation> = listOf(),
        val selectedTranslations: List<Translation> = listOf()
    )
}