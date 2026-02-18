package com.thesunnahrevival.sunnahassistant.utilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

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

    private var _downloadProgress = MutableSharedFlow<DownloadProgress>(replay = 1)

    val downloadProgress: Flow<DownloadProgress> = _downloadProgress.asSharedFlow()

    suspend fun updateUIProgress(progress: DownloadProgress) {
        _downloadProgress.emit(progress)
    }

    fun cancelDownload() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            updateUIProgress(Cancelled)
        }
    }

    sealed class DownloadProgress

    data object NotInitiated : DownloadProgress()

    data object Preparing : DownloadProgress()

    data class Downloading(
        val totalDownloadedSize: Float,
        val fileSize: Float,
        val unit: String
    ) : DownloadProgress()

    data object Extracting : DownloadProgress()

    data object Completed : DownloadProgress()
    data object Cancelled : DownloadProgress()
    data object Error : DownloadProgress()

    data object NetworkError : DownloadProgress()

}