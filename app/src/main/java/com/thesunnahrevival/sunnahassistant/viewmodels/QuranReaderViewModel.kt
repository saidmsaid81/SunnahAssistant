package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class QuranReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val mQuranRepository = QuranRepository.getInstance(getApplication())

    private var _lines = listOf<Line>()
    val lines: List<Line>
        get() = _lines

    var hasSeenDownloadFilesDialog = false;

    fun getLinesByPageNumber(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _lines = mQuranRepository.getLinesByPageNumber(pageNumber)
        }
    }

    suspend fun getLinesByAyahId(ayahId: Int) = mQuranRepository.getLinesByAyahId(ayahId)

    suspend fun getResourceLinks() = mQuranRepository.getResourceLinks()

    suspend fun downloadQuranPage(pageNumber: Int): String? {
        val resourceLinks = getResourceLinks()
        resourceLinks?.let {
            val url = "${it.quranPagesLink}/${pageNumber}.png"
            val response = mQuranRepository.downloadFile(url)

            if (response.isSuccessful) {
                val fileName = url.substringAfterLast("/")

                val file = File(getApplication<Application>().filesDir, fileName)

                response.body()?.let { responseBody ->
                    FileOutputStream(file).use { output ->
                        responseBody.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    return fileName
                }
            }
        }

        return null
    }
}