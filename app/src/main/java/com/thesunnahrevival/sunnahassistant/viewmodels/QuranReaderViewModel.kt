package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Line
import com.thesunnahrevival.sunnahassistant.data.model.ResourceLinks
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

    private var resourceLinks: ResourceLinks? = null

    suspend fun downloadQuranPage(pageNumber: Int): String? {
        if (resourceLinks == null) {
            resourceLinks = mQuranRepository.getResourceLinks()
        }
        resourceLinks?.let {
            val url = "${it.quranPagesLink}/${pageNumber}.png"
            val response = mQuranRepository.downloadFile(url)

            if (response.isSuccessful) {
                val fileName = url.substringAfterLast("/")

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
        }

        return null
    }

    suspend fun isHideDownloadFilePrompt() = mQuranRepository.isHideDownloadFilePrompt()
}