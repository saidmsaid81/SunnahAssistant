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
import com.thesunnahrevival.sunnahassistant.data.repositories.DownloadFileRepository
import com.thesunnahrevival.sunnahassistant.receivers.DownloadRetryReceiver
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOADS_NOTIFICATION_CHANNEL_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_WORK_TAG
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.*
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORT_EMAIL
import com.thesunnahrevival.sunnahassistant.utilities.roundTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt
import com.thesunnahrevival.sunnahassistant.data.repositories.FlagRepository

private const val FILE_SIZE_DECIMAL_PLACES = 1
private const val BYTES_IN_1_KB = 1024L
private const val BYTES_IN_1_MB = 1048576L
private const val PROGRESS_UPDATE_INTERVAL_MS = 3000L // 3 Seconds
private const val BUFFER_SIZE = 4096
private const val MAX_RESUME_ATTEMPTS = 5

private const val RESUME_ATTEMPTS_FLAG_KEY = "resume_attempts"

private const val DOWNLOADED_BYTES_FLAG_KEY = "downloaded_bytes"

class DownloadWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val downloadManager = DownloadManager.getInstance()
    private var tempZipFile: File? = null

    private val flagRepository = FlagRepository.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        return try {
            setForeground(createForegroundInfo(Preparing))

            downloadManager.updateUIProgress(Preparing)

            executeDownload()

            Result.success()
        } catch (e: CancellationException) {
            e.printStackTrace()
            cleanupTempFiles()
            clearDownloadState()
            clearResumeAttempts()
            downloadManager.updateUIProgress(Cancelled)
            Result.success()
        } catch (e: IOException) {
            e.printStackTrace()
            downloadManager.updateUIProgress(NetworkError)
            showErrorNotification(false)
            Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            downloadManager.updateUIProgress(Error)
            showErrorNotification(true)
            Result.failure()
        }
    }

    private suspend fun executeDownload() {
        val downloadFileRepository = DownloadFileRepository.getInstance(applicationContext)

        val resumeFromByte = if (getResumeAttempts() > MAX_RESUME_ATTEMPTS) {
            clearDownloadState()
            clearResumeAttempts()
            0
        } else {
            getDownloadState()
        }

        val response = downloadFileRepository.downloadFile(
            downloadFileRepository.getResourceLinks().body()?.quranZipFileLink ?: return,
            resumeFromByte
        )

        if (response?.isSuccessful == true) {
            response.body()?.let { responseBody ->
                tempZipFile = downloadFileToTemp(responseBody, resumeFromByte)
                tempZipFile?.let {
                    extractZipContents(it)
                    it.delete()
                }
                downloadManager.updateUIProgress(Completed)
                clearDownloadState()
                clearResumeAttempts()
                showSuccessNotification()
            } ?: throw IOException("Response body is null")
        } else {
            throw IOException("Download request failed with code: ${response?.code()}")
        }
    }

    private suspend fun downloadFileToTemp(responseBody: ResponseBody, resumeFromByte: Long = 0L): File {
        val contentLength = responseBody.contentLength()
        val isResume = resumeFromByte > 0

        if (isResume) {
            incrementResumeAttempts()
        }

        val totalBytes = contentLength + resumeFromByte
        val (totalSize, unit) = convertBytesToAppropriateUnit(totalBytes)

        // Calculate already downloaded amount for UI display
        val alreadyDownloadedSize = if (isResume) {
            if (unit == "KB") {
                (resumeFromByte.toFloat() / BYTES_IN_1_KB).roundTo(FILE_SIZE_DECIMAL_PLACES)
            } else {
                (resumeFromByte.toFloat() / BYTES_IN_1_MB).roundTo(FILE_SIZE_DECIMAL_PLACES)
            }
        } else 0f

        val initialProgress = Downloading(alreadyDownloadedSize, totalSize, unit)
        setForeground(createForegroundInfo(initialProgress))
        downloadManager.updateUIProgress(initialProgress)

        val tempFile = File(applicationContext.filesDir, "temp.zip")
        var downloadedBytes = resumeFromByte
        var lastUpdateTime = System.currentTimeMillis()

        withContext(Dispatchers.IO) {
            FileOutputStream(tempFile, isResume).use { output ->
                responseBody.byteStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()

                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        // Save progress periodically for resume capability
                        saveDownloadState(downloadedBytes)

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= PROGRESS_UPDATE_INTERVAL_MS) {
                            val downloadedSize = if (unit == "KB") {
                                (downloadedBytes.toFloat() / BYTES_IN_1_KB).roundTo(FILE_SIZE_DECIMAL_PLACES)
                            } else {
                                (downloadedBytes.toFloat() / BYTES_IN_1_MB).roundTo(FILE_SIZE_DECIMAL_PLACES)
                            }

                            val currentProgress = Downloading(downloadedSize, totalSize, unit)
                            setForeground(createForegroundInfo(currentProgress))
                            downloadManager.updateUIProgress(currentProgress)
                            lastUpdateTime = currentTime
                        }
                    }
                }
            }
        }

        // Final progress update
        val finalDownloadedSize = if (unit == "KB") {
            (downloadedBytes.toFloat() / BYTES_IN_1_KB).roundTo(FILE_SIZE_DECIMAL_PLACES)
        } else {
            (downloadedBytes.toFloat() / BYTES_IN_1_MB).roundTo(FILE_SIZE_DECIMAL_PLACES)
        }
        val finalProgress = Downloading(finalDownloadedSize, totalSize, unit)
        setForeground(createForegroundInfo(finalProgress))
        downloadManager.updateUIProgress(finalProgress)

        return tempFile
    }

    private suspend fun extractZipContents(zipFile: File) {
        setForeground(createForegroundInfo(Extracting))
        downloadManager.updateUIProgress(Extracting)

        withContext(Dispatchers.IO) {
            ZipInputStream(zipFile.inputStream()).use { zipInputStream ->
                var zipEntry = zipInputStream.nextEntry
                val buffer = ByteArray(BUFFER_SIZE)

                while (zipEntry != null) {
                    ensureActive()

                    val newFile = File(applicationContext.filesDir, zipEntry.name)

                    if (zipEntry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()

                        FileOutputStream(newFile).use { fileOutputStream ->
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
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

    private fun cleanupTempFiles() {
        tempZipFile?.delete()
    }

    private suspend fun saveDownloadState(downloadedBytes: Long) {
        flagRepository.setFlag(DOWNLOADED_BYTES_FLAG_KEY, downloadedBytes)
    }

    private suspend fun clearDownloadState() {
        flagRepository.clearLongFlag(DOWNLOADED_BYTES_FLAG_KEY)
    }

    private suspend fun getDownloadState(): Long {
        return flagRepository.getLongFlag(DOWNLOADED_BYTES_FLAG_KEY) ?: 0
    }

    private suspend fun getResumeAttempts(): Int {
        return flagRepository.getIntFlag(RESUME_ATTEMPTS_FLAG_KEY) ?: 0
    }

    private suspend fun incrementResumeAttempts(): Int {
        val attempts = getResumeAttempts() + 1
        flagRepository.setFlag(RESUME_ATTEMPTS_FLAG_KEY, attempts)
        return attempts
    }

    private suspend fun clearResumeAttempts() {
        flagRepository.clearIntFlag(RESUME_ATTEMPTS_FLAG_KEY)
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

    private fun showSuccessNotification() {
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

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DOWNLOAD_COMPLETE_NOTIFICATION_ID, notification)
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
    }

    private fun showErrorNotification(isGeneralError: Boolean) {
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
            content = if (isGeneralError)
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

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DOWNLOAD_COMPLETE_NOTIFICATION_ID, notification)
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
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