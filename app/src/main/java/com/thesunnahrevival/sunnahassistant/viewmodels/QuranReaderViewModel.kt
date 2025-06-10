package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuranReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val mQuranRepository = QuranRepository.getInstance(getApplication())

    private var _lines = listOf<Line>()
    val lines: List<Line>
        get() = _lines

    fun getLinesByPageNumber(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _lines = mQuranRepository.getLinesByPageNumber(pageNumber)
        }
    }

    suspend fun getLinesByAyahId(ayahId: Int) = mQuranRepository.getLinesByAyahId(ayahId)
}