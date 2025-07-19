package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.Line
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranPageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class QuranReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val quranPageRepository = QuranPageRepository.getInstance(getApplication())

    private var _lines = listOf<Line>()
    val lines: List<Line>
        get() = _lines

    var hasSeenDownloadFilesDialog = false

    fun getLinesByPageNumber(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _lines = quranPageRepository.getLinesByPageNumber(pageNumber)
        }
    }

    suspend fun getLinesByAyahId(ayahId: Int) = quranPageRepository.getLinesByAyahId(ayahId)


    suspend fun downloadQuranPage(pageNumber: Int): String? {
        val response = quranPageRepository.downloadQuranPage(pageNumber)

        if (response?.isSuccessful == true) {
            val fileName = "${pageNumber}.png"

            val quranPagesDir = File(getApplication<Application>().filesDir, "quran_pages")
            if (!quranPagesDir.exists()) {
                quranPagesDir.mkdir()
            }
            val file = File(quranPagesDir, fileName)

            response.body()?.let { responseBody ->
                FileOutputStream(file).use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                return fileName
            }
        }

        return null
    }

    suspend fun isHideDownloadFilePrompt() = quranPageRepository.isHideDownloadFilePrompt()
}