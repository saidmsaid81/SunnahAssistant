package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val quranRepository = QuranRepository.getInstance(application)

    fun disableDownloadFilesPrompt() {
        viewModelScope.launch(Dispatchers.IO) {
            quranRepository.updateHideDownloadFilePrompt(true)
        }
    }

}