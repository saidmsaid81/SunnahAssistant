package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.data.repositories.DownloadFileRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.FlagRepository
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_WORK_TAG
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Cancelled
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Completed
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Error
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.NetworkError
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.NotInitiated
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY
import com.thesunnahrevival.sunnahassistant.utilities.NetworkManager
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadCancelledState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadCompleteState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadErrorState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadInProgressState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadNetworkErrorState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadPromptState
import com.thesunnahrevival.sunnahassistant.viewmodels.state.DownloadUIState
import com.thesunnahrevival.sunnahassistant.workers.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadFileRepository = DownloadFileRepository.getInstance(application)
    private val flagRepository = FlagRepository.getInstance(application)

    private val downloadManager = DownloadManager.getInstance()

    val networkCapabilities: StateFlow<NetworkCapabilities?> = NetworkManager.getInstance(getApplication()).networkCapabilitiesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val downloadUIState: StateFlow<DownloadUIState> =
        downloadManager.downloadProgress.map { downloadProgress ->
            when (downloadProgress) {
                NotInitiated -> DownloadPromptState

                Completed -> DownloadCompleteState

                Cancelled -> DownloadCancelledState

                Error -> DownloadErrorState

                NetworkError -> DownloadNetworkErrorState

                else -> DownloadInProgressState(downloadProgress)
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
        //Clear any download completion/error notifications
        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(DOWNLOAD_COMPLETE_NOTIFICATION_ID)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DOWNLOAD_WORK_TAG)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(downloadRequest)
    }

    fun cancelDownload() {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
        downloadManager.cancelDownload()
    }

    fun incrementNotificationRequestCount() {
        viewModelScope.launch(Dispatchers.IO) {
            val notificationPermissionRequestCount =
                flagRepository.getLongFlag(NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY) ?: 0
            if (notificationPermissionRequestCount != -1L) {
                flagRepository.setFlag(
                    NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY,
                    (notificationPermissionRequestCount + 1)
                )
            }
        }
    }


}