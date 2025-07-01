package com.thesunnahrevival.sunnahassistant.data

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

    suspend fun downloadFile(): Response<ResponseBody>? {
        val response = resourceApiRestApi.getResourceLinks()
        response.body()?.let { resourceLink ->
            return resourceApiRestApi.downloadFile(resourceLink.quranZipFileLink)
        }
        return null
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