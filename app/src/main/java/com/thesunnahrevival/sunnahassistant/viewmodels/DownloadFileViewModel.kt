package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.data.DownloadFileRepository
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Cancelled
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Completed
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.DownloadProgress
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.NotInitiated
import com.thesunnahrevival.sunnahassistant.workers.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DOWNLOAD_WORK_TAG = "download_work"

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadFileRepository = DownloadFileRepository.getInstance(application)

    private val downloadManager = DownloadManager.getInstance()

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
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DOWNLOAD_WORK_TAG)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(downloadRequest)
    }

    fun cancelDownload() {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
        viewModelScope.launch {
            downloadManager.cancelDownload()
        }
    }

    sealed class DownloadUIState

    data object DownloadPromptState : DownloadUIState()

    data class DownloadInProgressState(val downloadProgress: DownloadProgress) : DownloadUIState()

    data object DownloadCompleteState : DownloadUIState()

    data object DownloadCancelledState : DownloadUIState()

}