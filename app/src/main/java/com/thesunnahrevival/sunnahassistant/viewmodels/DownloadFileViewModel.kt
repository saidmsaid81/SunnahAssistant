package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.DownloadFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadFileRepository = DownloadFileRepository.getInstance(application)

    val downloadUIState = downloadFileRepository.downloadStatus.map { downloadProgress ->
        when (downloadProgress) {
            DownloadFileRepository.NotInitiated -> DownloadPromptState
            DownloadFileRepository.Preparing -> DownloadInProgressState(downloadProgress)
            is DownloadFileRepository.Downloading -> DownloadInProgressState(downloadProgress)
            DownloadFileRepository.Extracting -> DownloadInProgressState(downloadProgress)
            DownloadFileRepository.Completed -> DownloadCompleteState
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadPromptState
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

    fun downloadAndExtractZip() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadFileRepository.downloadAndExtractZip()
        }
    }

    sealed class DownloadUIState

    data object DownloadPromptState : DownloadUIState()

    data class DownloadInProgressState(val downloadStatus: DownloadFileRepository.DownloadStatus) :
        DownloadUIState()

    data object DownloadCompleteState : DownloadUIState()

}