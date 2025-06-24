package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class AyahTranslationViewModel(application: Application) : TranslationViewModel(application) {

    private val mQuranRepository = QuranRepository.getInstance(getApplication())

    private val _selectedAyah = MutableStateFlow<FullAyahDetails?>(null)
    val selectedAyah = _selectedAyah.asStateFlow()

    private val _visibleFootnotes = mutableStateMapOf<String, Footnote>()
    val visibleFootnotes: SnapshotStateMap<String, Footnote> = _visibleFootnotes

    fun setSelectedAyah(ayah: FullAyahDetails) {
        _selectedAyah.update { ayah }
    }

    suspend fun getAyahById(ayahId: Int) = mQuranRepository.getFullAyahDetailsById(ayahId)

    fun toggleFootnote(ayahTranslationId: Int, footnoteNumber: Int) {
        val footnoteKey = "$ayahTranslationId-$footnoteNumber"

        if (_visibleFootnotes.remove(footnoteKey) == null) {
            viewModelScope.launch {
                mQuranRepository.getFootnote(ayahTranslationId, footnoteNumber)?.let { footnote ->
                    _visibleFootnotes[footnoteKey] = footnote
                }
            }
        }
    }

}
