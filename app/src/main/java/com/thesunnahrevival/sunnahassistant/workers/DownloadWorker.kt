package com.thesunnahrevival.sunnahassistant.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.receivers.DownloadRetryReceiver
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOADS_NOTIFICATION_CHANNEL_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.*
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORT_EMAIL
import com.thesunnahrevival.sunnahassistant.viewmodels.DOWNLOAD_WORK_TAG
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DownloadWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val downloadManager = DownloadManager.getInstance()

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(Preparing))

        downloadManager.downloadFile(applicationContext)

        val downloadCompletion = CompletableDeferred<Result>()

        downloadManager.downloadProgress.collect { downloadProgress ->
            when (downloadProgress) {
                Completed -> {
                    val notification = getNotification(
                        title = applicationContext.getString(R.string.download_complete),
                        content = applicationContext.getString(R.string.tap_to_read_quran),
                        channelId = DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID,
                        smallIconRes = R.drawable.ic_alarm,
                        pendingIntent = NavDeepLinkBuilder(applicationContext)
                            .setGraph(R.navigation.navigation)
                            .setDestination(R.id.surahList)
                            .createPendingIntent()
                    )

                    val notificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(DOWNLOAD_COMPLETE_NOTIFICATION_ID, notification)
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
                    downloadCompletion.complete(Result.success())
                }
                Error, NetworkError -> {
                    val retryIntent = android.content.Intent(applicationContext, DownloadRetryReceiver::class.java)
                    val retryPendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        retryIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val retryAction = NotificationCompat.Action(
                        R.drawable.ic_retry,
                        applicationContext.getString(R.string.try_again),
                        retryPendingIntent
                    )

                    val notification = getNotification(
                        title = applicationContext.getString(R.string.downloading_failed),
                        content = if (downloadProgress is Error)
                            applicationContext.getString(R.string.an_error_occurred, SUPPORT_EMAIL)
                        else
                            applicationContext.getString(R.string.network_error),
                        channelId = DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID,
                        smallIconRes = R.drawable.ic_alarm,
                        pendingIntent = NavDeepLinkBuilder(applicationContext)
                            .setGraph(R.navigation.navigation)
                            .setDestination(R.id.resourcesFragment)
                            .createPendingIntent(),
                        actions = listOf(retryAction)
                    )

                    val notificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(DOWNLOAD_COMPLETE_NOTIFICATION_ID, notification)
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
                    downloadCompletion.complete(Result.failure())
                }

                Cancelled -> {
                    downloadCompletion.complete(Result.success())
                }

                else -> {
                    setForeground(createForegroundInfo(downloadProgress))
                }

            }
        }

        return downloadCompletion.await()
    }

    private fun createForegroundInfo(downloadProgress: DownloadProgress): ForegroundInfo {
        createNotificationChannel()

        val action = getAction()
        val notification: Notification = when (downloadProgress) {
            is Downloading -> {
                val fileSize = downloadProgress.fileSize
                val totalDownloadedSize = downloadProgress.totalDownloadedSize
                val progress = ((totalDownloadedSize / fileSize) * 100).roundToInt()

                getNotification(
                    title = applicationContext.getString(R.string.downloading_quran_files_please_wait),
                    content = applicationContext.getString(
                        R.string.downloaded,
                        totalDownloadedSize.toString(),
                        fileSize.toString(),
                        downloadProgress.unit
                    ),
                    channelId = DOWNLOADS_NOTIFICATION_CHANNEL_ID,
                    smallIconRes = R.drawable.ic_downloading,
                    notificationProgress = NotificationProgress(
                        max = 100,
                        progress = progress,
                        indeterminate = false,
                        ongoing = true
                    ),
                    actions = listOf(action)
                )
            }

            Extracting -> getNotification(
                title = applicationContext.getString(R.string.downloading_quran_files_please_wait),
                content = applicationContext.getString(R.string.extracting),
                channelId = DOWNLOADS_NOTIFICATION_CHANNEL_ID,
                smallIconRes = R.drawable.ic_downloading,
                notificationProgress = NotificationProgress(
                    100,
                    0,
                    indeterminate = true,
                    ongoing = true
                ),
                actions = listOf(action)
            )

            else -> getNotification(
                title = applicationContext.getString(R.string.downloading_quran_files_please_wait),
                content = applicationContext.getString(R.string.calculating),
                channelId = DOWNLOADS_NOTIFICATION_CHANNEL_ID,
                smallIconRes = R.drawable.ic_downloading,
                notificationProgress = NotificationProgress(
                    100,
                    0,
                    indeterminate = true,
                    ongoing = true
                ),
                actions = listOf(action)
            )

        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                DOWNLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }

    private fun getAction(): NotificationCompat.Action {
        val intent = android.content.Intent(
            applicationContext,
            com.thesunnahrevival.sunnahassistant.receivers.DownloadCancelReceiver::class.java
        )
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            applicationContext.getString(R.string.cancel),
            pendingIntent
        )
        return action
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadsChannel = NotificationChannel(
                DOWNLOADS_NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.file_downloads_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            downloadsChannel.description =
                applicationContext.getString(R.string.used_in_showing_download_progress_when_downloading_files_in_sunnah_assistant)

            val downloadCompleteChannel = NotificationChannel(
                DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.download_complete),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            downloadCompleteChannel.description =
                applicationContext.getString(R.string.notifications_for_download_completion_and_errors)

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(downloadsChannel)
            notificationManager.createNotificationChannel(downloadCompleteChannel)
        }
    }

    private fun getNotification(
        title: String,
        content: String,
        channelId: String,
        smallIconRes: Int,
        notificationProgress: NotificationProgress? = null,
        pendingIntent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
    ): Notification {
        val notification =
            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(applicationContext.getString(R.string.app_name))
                .setSmallIcon(smallIconRes)
                .setContentIntent(pendingIntent)

        if (notificationProgress != null) {
            notification.setProgress(
                notificationProgress.max,
                notificationProgress.progress,
                notificationProgress.indeterminate
            )
            notification.setOngoing(notificationProgress.ongoing)
        }

        for (action in actions) {
            notification.addAction(action)
        }

        return notification.build()
    }

    private data class NotificationProgress(
        val max: Int,
        val progress: Int,
        val indeterminate: Boolean,
        val ongoing: Boolean
    )


}