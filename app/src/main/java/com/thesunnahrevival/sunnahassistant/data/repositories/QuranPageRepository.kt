package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.LineDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import okhttp3.ResponseBody
import retrofit2.Response

class QuranPageRepository private constructor(
    private val applicationContext: Context
) {
    companion object {
        @Volatile
        private var instance: QuranPageRepository? = null

        fun getInstance(context: Context): QuranPageRepository {
            return instance ?: synchronized(this) {
                instance ?: QuranPageRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val downloadFileRepository = DownloadFileRepository.getInstance(applicationContext)

    private val lineDao: LineDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).lineDao()

    private var resourceLinks: String? = null

    suspend fun getLinesByPageNumber(pageNumber: Int) = lineDao.getLineByPageNumber(pageNumber)

    suspend fun getLinesByAyahId(ayahId: Int) = lineDao.getLineByAyahId(ayahId)

    suspend fun downloadQuranPage(pageNumber: Int): Response<ResponseBody>? {
        resourceLinks = downloadFileRepository.getResourceLinks().body()?.quranPagesLink
        return downloadFileRepository.downloadFile(("$resourceLinks/$pageNumber.png"))
    }

    suspend fun isHideDownloadFilePrompt() = downloadFileRepository.isHideDownloadFilePrompt()



}