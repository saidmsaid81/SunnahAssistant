package com.thesunnahrevival.sunnahassistant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_WORK_TAG
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager

class DownloadCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        DownloadManager.getInstance().cancelDownload()
        WorkManager.getInstance(context).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
    }
}