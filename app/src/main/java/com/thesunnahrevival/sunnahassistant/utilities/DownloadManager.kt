package com.thesunnahrevival.sunnahassistant.utilities

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.DownloadFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream


private const val FILE_SIZE_DECIMAL_PLACES = 1
private const val BYTES_IN_1_KB = 1024L
private const val BYTES_IN_1_MB = 1048576L

class DownloadManager private constructor() {

    companion object {
        @Volatile
        private var instance: DownloadManager? = null

        fun getInstance(): DownloadManager {
            return instance ?: synchronized(this) {
                instance ?: DownloadManager().also { instance = it }
            }
        }
    }

    private var _downloadProgress = MutableSharedFlow<DownloadProgress>()
    private var tempZipFile: File? = null
    private var currentState: DownloadProgress = NotInitiated

    val downloadProgress: Flow<DownloadProgress> = _downloadProgress

    suspend fun downloadFile(context: Context) {
        val downloadFileRepository = DownloadFileRepository.getInstance(context)
        try {
            updateProgress(Preparing)
            val response = downloadFileRepository.downloadFile()

            if (response?.isSuccessful == true) {
                response.body()?.let { responseBody: ResponseBody ->
                    tempZipFile = generateTempZipFile(responseBody, context)

                    tempZipFile?.let {
                        extractZipContents(it, context)
                    }

                    tempZipFile?.delete()

                    updateProgress(Completed)
                }
            }
        } catch (e: Exception) {
            tempZipFile?.delete()
            e.printStackTrace()
        }
    }

    private suspend fun updateProgress(progress: DownloadProgress) {
        currentState = progress
        _downloadProgress.emit(progress)
    }

    suspend fun cancelDownload() {
        tempZipFile?.delete()
        updateProgress(Cancelled)
    }

    private suspend fun generateTempZipFile(
        responseBody: ResponseBody,
        application: Context
    ): File {
        val totalBytes = responseBody.contentLength()

        val (totalSize, unit) = convertBytesToAppropriateUnit(totalBytes)

        _downloadProgress.emit(Downloading(0f, totalSize, unit))

        val tempZipFile = File(application.filesDir, "temp.zip")
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

                            _downloadProgress.emit(Downloading(downloadedSize, totalSize, unit))
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
        _downloadProgress.emit(Downloading(finalDownloadedSize, totalSize, unit))

        return tempZipFile
    }

    private suspend fun extractZipContents(tempZipFile: File, application: Context) {
        _downloadProgress.emit(Extracting)
        withContext(Dispatchers.IO) {
            ZipInputStream(tempZipFile.inputStream())
                .use { zipInputStream ->
                    var zipEntry = zipInputStream.nextEntry
                    val buffer = ByteArray(1024)

                    while (zipEntry != null) {
                        ensureActive()
                        val newFile =
                            File(
                                application.filesDir,
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

    sealed class DownloadProgress

    data object NotInitiated : DownloadProgress()

    data object Preparing : DownloadProgress()

    data class Downloading(
        val totalDownloadedSize: Float,
        val totalFileSize: Float,
        val unit: String
    ) : DownloadProgress()

    data object Extracting : DownloadProgress()

    data object Completed : DownloadProgress()
    data object Cancelled : DownloadProgress()

}