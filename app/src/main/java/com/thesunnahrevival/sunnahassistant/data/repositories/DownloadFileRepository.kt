package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AppSettingsDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import okhttp3.ResponseBody
import retrofit2.Response


class DownloadFileRepository private constructor(
    private val applicationContext: Context
) {

    private val appSettingsDao: AppSettingsDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).appSettingsDao()

    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)

    suspend fun updateHideDownloadFilePrompt(value: Boolean) =
        appSettingsDao.updateHideDownloadFilePrompt(value)

    suspend fun isHideDownloadFilePrompt() = appSettingsDao.isHideDownloadFilePrompt()

    suspend fun getResourceLinks() = resourceApiRestApi.getResourceLinks()

    suspend fun downloadFile(url: String, rangeStart: Long = 0): Response<ResponseBody>? {
        return try {
            if (rangeStart > 0) {
                val rangeHeader = "bytes=$rangeStart-"
                resourceApiRestApi.downloadFileWithRange(rangeHeader, url)
            } else {
                resourceApiRestApi.downloadFile(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
}