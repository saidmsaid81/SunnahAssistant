package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.utilities.roundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

private const val FILE_SIZE_DECIMAL_PLACES = 1
private const val BYTES_IN_1_KB = 1024L
private const val BYTES_IN_1_MB = 1048576L

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val mQuranRepository = QuranRepository.getInstance(application)

    private val _downloadStep = MutableStateFlow(DownloadStep.DOWNLOAD_PROMPT)
    private val _totalMegaBytesDownloaded = MutableStateFlow(0f)
    private val _totalFileSize = MutableStateFlow(0f)
    private val _fileSizeUnit = MutableStateFlow("MB")

    val totalMegaBytesDownloaded: StateFlow<Float> = _totalMegaBytesDownloaded
    val totalFileSize: StateFlow<Float> = _totalFileSize
    val fileSizeUnit: StateFlow<String> = _fileSizeUnit
    val downloadStep: StateFlow<DownloadStep> = _downloadStep

    fun showDownloadInProgressUI() {
        _downloadStep.value = DownloadStep.DOWNLOAD_IN_PROGRESS
        downloadAndExtractZip()
    }

    fun disableDownloadFilesPrompt() {
        viewModelScope.launch(Dispatchers.IO) {
            mQuranRepository.updateHideDownloadFilePrompt(true)
        }
    }


    private fun downloadAndExtractZip() {
        viewModelScope.launch(Dispatchers.IO) {
            val resourceLinks = mQuranRepository.getResourceLinks()
            resourceLinks?.let {
                val zipUrl = it.quranZipFileLink
                try {
                    val response = mQuranRepository.downloadFile(zipUrl)

                    if (response.isSuccessful) {
                        val tempZipFile = File(getApplication<Application>().filesDir, "temp.zip")

                        response.body()?.let { responseBody ->
                            val totalBytes = responseBody.contentLength()

                            val (totalSize, unit) = convertBytesToAppropriateUnit(totalBytes)
                            _totalFileSize.value = totalSize
                            _fileSizeUnit.value = unit

                            var downloadedBytes = 0L

                            FileOutputStream(tempZipFile).use { output ->
                                responseBody.byteStream().use { input ->
                                    val buffer = ByteArray(8192)
                                    var bytesRead: Int

                                    while (input.read(buffer).also { bytesRead = it } != -1) {
                                        output.write(buffer, 0, bytesRead)
                                        downloadedBytes += bytesRead

                                        val downloadedSize = if (unit == "kB") {
                                            (downloadedBytes.toFloat() / BYTES_IN_1_KB).roundTo(
                                                FILE_SIZE_DECIMAL_PLACES
                                            )
                                        } else {
                                            (downloadedBytes.toFloat() / BYTES_IN_1_MB).roundTo(
                                                FILE_SIZE_DECIMAL_PLACES
                                            )
                                        }
                                        _totalMegaBytesDownloaded.value = downloadedSize
                                    }
                                }
                            }

                            // Extract zip contents
                            java.util.zip.ZipInputStream(tempZipFile.inputStream())
                                .use { zipInputStream ->
                                    var zipEntry = zipInputStream.nextEntry
                                    val buffer = ByteArray(1024)

                                    while (zipEntry != null) {
                                        val newFile =
                                            File(
                                                getApplication<Application>().filesDir,
                                                zipEntry.name
                                            )

                                        // Create directories if needed
                                        if (zipEntry.isDirectory) {
                                            newFile.mkdirs()
                                        } else {
                                            // Create parent directories if they don't exist
                                            newFile.parentFile?.mkdirs()

                                            // Extract file
                                            FileOutputStream(newFile).use { fileOutputStream ->
                                                var len: Int
                                                while (zipInputStream.read(buffer)
                                                        .also { len = it } > 0
                                                ) {
                                                    fileOutputStream.write(buffer, 0, len)
                                                }
                                            }
                                        }
                                        zipEntry = zipInputStream.nextEntry
                                    }
                                }

                            // Clean up - delete temporary zip file
                            tempZipFile.delete()

                            _downloadStep.value = DownloadStep.DOWNLOAD_COMPLETE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun convertBytesToAppropriateUnit(bytes: Long): Pair<Float, String> {
        return if (bytes < BYTES_IN_1_MB) {
            val kb = bytes.toFloat() / BYTES_IN_1_KB
            Pair(kb.roundTo(FILE_SIZE_DECIMAL_PLACES), "KB")
        } else {
            val mb = bytes.toFloat() / BYTES_IN_1_MB
            Pair(mb.roundTo(FILE_SIZE_DECIMAL_PLACES), "MB")
        }
    }

    enum class DownloadStep {
        DOWNLOAD_PROMPT, DOWNLOAD_IN_PROGRESS, DOWNLOAD_COMPLETE
    }

}