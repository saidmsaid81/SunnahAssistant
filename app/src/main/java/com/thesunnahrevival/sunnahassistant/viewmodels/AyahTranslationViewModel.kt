package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class AyahTranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val mQuranRepository = QuranRepository.getInstance(getApplication())

    private val _selectedAyah = MutableStateFlow<FullAyahDetails?>(null)
    val selectedAyah = _selectedAyah.asStateFlow()
    val translations = mQuranRepository.getTranslations()

    val selectedTranslations = translations.map { translationsList ->
        translationsList.filter { it.selected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _visibleFootnotes = mutableStateMapOf<String, Boolean>()
    val visibleFootnotes: Map<String, Boolean> = _visibleFootnotes


    fun toggleTranslationSelection(translation: Translation) {
        translation.selected = !translation.selected
        viewModelScope.launch(Dispatchers.IO) {
            mQuranRepository.updateTranslation(translation)
        }
    }

    fun setSelectedAyah(ayah: FullAyahDetails) {
        _selectedAyah.update { ayah }
    }

    suspend fun getAyahById(ayahId: Int) = mQuranRepository.getFullAyahDetailsById(ayahId)

    fun toggleFootnote(ayahTranslationId: Int, footnoteNumber: Int) {
        val footnoteKey = "$ayahTranslationId-$footnoteNumber"
        _visibleFootnotes[footnoteKey] = !(_visibleFootnotes[footnoteKey] ?: false)
    }

}