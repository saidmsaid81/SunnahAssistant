package com.thesunnahrevival.sunnahassistant.data

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AppSettingsDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import com.thesunnahrevival.sunnahassistant.utilities.roundTo
import kotlinx.coroutines.Dispatchers
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

class DownloadFileRepository private constructor(
    private val applicationContext: Context
) {

    private val appSettingsDao: AppSettingsDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).appSettingsDao()

    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)

    private val _downloadStatus = MutableSharedFlow<DownloadStatus>().apply {
        tryEmit(NotInitiated)
    }

    val downloadStatus: Flow<DownloadStatus> = _downloadStatus

    suspend fun updateHideDownloadFilePrompt(value: Boolean) =
        appSettingsDao.updateHideDownloadFilePrompt(value)

    suspend fun downloadAndExtractZip() {
        val resourceLinks = resourceApiRestApi.getResourceLinks().body()

        _downloadStatus.emit(Preparing)

        resourceLinks?.let { links ->
            val zipUrl = links.quranZipFileLink
            try {
                val response = resourceApiRestApi.downloadFile(zipUrl)

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val totalBytes = responseBody.contentLength()

                        val (totalSize, unit) = convertBytesToAppropriateUnit(totalBytes)
                        _downloadStatus.emit(Downloading(0f, totalSize, unit))

                        val tempZipFile = generateTempZipFile(responseBody, unit, totalSize)

                        _downloadStatus.emit(Extracting)

                        extractZipContents(tempZipFile)

                        tempZipFile.delete()

                        _downloadStatus.emit(Completed)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private suspend fun generateTempZipFile(
        responseBody: ResponseBody,
        unit: String,
        totalSize: Float
    ): File {
        val tempZipFile = File(applicationContext.filesDir, "temp.zip")
        var downloadedBytes = 0L
        var lastEmissionTime = 3000L

        withContext(Dispatchers.IO) {
            FileOutputStream(tempZipFile).use { output ->
                responseBody.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
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

                            _downloadStatus.emit(Downloading(downloadedSize, totalSize, unit))
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
        _downloadStatus.emit(Downloading(finalDownloadedSize, totalSize, unit))

        return tempZipFile
    }

    private fun extractZipContents(tempZipFile: File) {
        ZipInputStream(tempZipFile.inputStream())
            .use { zipInputStream ->
                var zipEntry = zipInputStream.nextEntry
                val buffer = ByteArray(1024)

                while (zipEntry != null) {
                    val newFile =
                        File(
                            applicationContext.filesDir,
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

    companion object {
        @Volatile
        private var instance: DownloadFileRepository? = null

        fun getInstance(context: Context): DownloadFileRepository {
            return instance ?: synchronized(this) {
                instance ?: DownloadFileRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    sealed class DownloadStatus

    data object NotInitiated : DownloadStatus()

    data object Preparing : DownloadStatus()

    data class Downloading(
        val totalDownloadedSize: Float,
        val totalFileSize: Float,
        val unit: String
    ) : DownloadStatus()

    data object Extracting : DownloadStatus()

    data object Completed : DownloadStatus()
}