package com.thesunnahrevival.sunnahassistant.viewmodels.state

import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager

sealed class DownloadUIState

data object DownloadPromptState : DownloadUIState()

data class DownloadInProgressState(val downloadProgress: DownloadManager.DownloadProgress) : DownloadUIState()

data object DownloadCompleteState : DownloadUIState()

data object DownloadCancelledState : DownloadUIState()

data object DownloadErrorState : DownloadUIState()

data object DownloadNetworkErrorState : DownloadUIState()