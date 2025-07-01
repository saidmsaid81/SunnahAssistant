package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.DownloadFileRepository
import com.thesunnahrevival.sunnahassistant.utilities.roundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val FILE_SIZE_DECIMAL_PLACES = 1
private const val BYTES_IN_1_KB = 1024L
private const val BYTES_IN_1_MB = 1048576L

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadFileRepository = DownloadFileRepository.getInstance(application)

    private val _downloadUIState = MutableStateFlow<DownloadUIState>(DownloadPromptState)

    val downloadUIState: StateFlow<DownloadUIState> = _downloadUIState

    private var downloadJob: Job? = null

    private var tempZipFile: File? = null

    fun disableDownloadFilesPrompt() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadFileRepository.updateHideDownloadFilePrompt(true)
        }
    }

    fun enableDownloadFilesPrompt() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadFileRepository.updateHideDownloadFilePrompt(false)
        }
    }

    fun downloadQuranFiles() {
        if (downloadJob != null && downloadJob?.isActive == true) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            downloadJob = coroutineScope {
                launch {
                    try {
                        _downloadUIState.value = DownloadInProgressState(Preparing)
                        val response = downloadFileRepository.downloadFile() ?: return@launch

                        if (response.isSuccessful) {
                            response.body()?.let { responseBody ->
                                tempZipFile = generateTempZipFile(responseBody)

                                tempZipFile?.let {
                                    extractZipContents(it)
                                }

                                tempZipFile?.delete()

                                _downloadUIState.value = DownloadCompleteState
                            }
                        }
                    } catch (e: Exception) {
                        tempZipFile?.delete()
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            downloadJob?.cancel()
            downloadJob = null
            tempZipFile?.delete()
            _downloadUIState.value = DownloadCancelled
        }
    }

    private suspend fun generateTempZipFile(
        responseBody: ResponseBody
    ): File {
        val totalBytes = responseBody.contentLength()

        val (totalSize, unit) = convertBytesToAppropriateUnit(totalBytes)

        _downloadUIState.value = DownloadInProgressState(Downloading(0f, totalSize, unit))

        val tempZipFile = File(getApplication<Application>().filesDir, "temp.zip")
        var downloadedBytes = 0L
        var lastEmissionTime = 3000L

        withContext(Dispatchers.IO) {
            FileOutputStream(tempZipFile).use { output ->
                responseBody.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val currentTime = System.currentTimeMillis()
                        val timeElapsed = currentTime - lastEmissionTime >= 3000 // 3 seconds

                        if (timeElapsed) {
                            val downloadedSize = if (unit == "KB") {
                                (downloadedBytes.toFloat() / BYTES_IN_1_KB).roundTo(
                                    FILE_SIZE_DECIMAL_PLACES
                                )
                            } else {
                                (downloadedBytes.toFloat() / BYTES_IN_1_MB).roundTo(
                                    FILE_SIZE_DECIMAL_PLACES
                                )
                            }

                            _downloadUIState.value = DownloadInProgressState(
                                Downloading(
                                    downloadedSize,
                                    totalSize,
                                    unit
                                )
                            )
                            lastEmissionTime = currentTime
                        }
                    }
                }
            }
        }

        // Emit final progress
        val finalDownloadedSize = if (unit == "KB") {
            (downloadedBytes.toFloat() / BYTES_IN_1_KB).roundTo(FILE_SIZE_DECIMAL_PLACES)
        } else {
            (downloadedBytes.toFloat() / BYTES_IN_1_MB).roundTo(FILE_SIZE_DECIMAL_PLACES)
        }
        _downloadUIState.value =
            DownloadInProgressState(Downloading(finalDownloadedSize, totalSize, unit))

        return tempZipFile
    }

    private suspend fun extractZipContents(tempZipFile: File) {
        _downloadUIState.value = DownloadInProgressState(Extracting)
        withContext(Dispatchers.IO) {
            ZipInputStream(tempZipFile.inputStream())
                .use { zipInputStream ->
                    var zipEntry = zipInputStream.nextEntry
                    val buffer = ByteArray(1024)

                    while (zipEntry != null) {
                        ensureActive()
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
                                    ensureActive()
                                    fileOutputStream.write(buffer, 0, len)
                                }
                            }
                        }
                        zipEntry = zipInputStream.nextEntry
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

    sealed class DownloadUIState

    data object DownloadPromptState : DownloadUIState()

    data class DownloadInProgressState(val downloadStatus: DownloadStatus) :
        DownloadUIState()

    data object DownloadCompleteState : DownloadUIState()

    data object DownloadCancelled : DownloadUIState()

    sealed class DownloadStatus

    data object Preparing : DownloadStatus()

    data class Downloading(
        val totalDownloadedSize: Float,
        val totalFileSize: Float,
        val unit: String
    ) : DownloadStatus()

    data object Extracting : DownloadStatus()

//    val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
//        .addTag("download_work")
//        .build()
//
//    WorkManager.getInstance(getApplication()).enqueue(downloadRequest)

}