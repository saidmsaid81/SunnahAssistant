package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.DownloadFileRepository
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Cancelled
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Completed
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.DownloadProgress
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.NotInitiated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadFileRepository = DownloadFileRepository.getInstance(application)

    private val downloadManager = DownloadManager()

    val downloadUIState: StateFlow<DownloadUIState> =
        downloadManager.downloadProgress.map { downloadProgress ->
            when (downloadProgress) {
                NotInitiated -> {
                    DownloadPromptState
                }

                Completed -> {
                    DownloadCompleteState
                }

                Cancelled -> {
                    DownloadCancelledState
                }

                else -> {
                    DownloadInProgressState(downloadProgress)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadInProgressState(NotInitiated)
        )

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
        downloadManager.downloadFile(getApplication(), viewModelScope)
    }

    fun cancelDownload() {
        viewModelScope.launch {
            downloadManager.cancelDownload()
        }
    }

    sealed class DownloadUIState

    data object DownloadPromptState : DownloadUIState()

    data class DownloadInProgressState(val downloadProgress: DownloadProgress) : DownloadUIState()

    data object DownloadCompleteState : DownloadUIState()

    data object DownloadCancelledState : DownloadUIState()

//    val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
//        .addTag("download_work")
//        .build()
//
//    WorkManager.getInstance(getApplication()).enqueue(downloadRequest)

}