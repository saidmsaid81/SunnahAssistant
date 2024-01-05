package com.thesunnahrevival.sunnahassistant.data

import android.content.Context
import androidx.paging.PagingSource
import com.prof18.rssparser.RssParser
import com.thesunnahrevival.sunnahassistant.data.local.DailyHadithDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith
import com.thesunnahrevival.sunnahassistant.utilities.THE_SUNNAH_REVIVAL_RSS_FEED
import java.text.SimpleDateFormat
import java.util.Date

class DailyHadithRepository private constructor(private val applicationContext: Context) {

    private val mDailyHadithDao: DailyHadithDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).dailyHadithDao()

    fun getDailyHadithFromTheSunnahRevivalBlog(): PagingSource<Int, DailyHadith> {
        return mDailyHadithDao.getDailyHadithList()
    }

    suspend fun fetchDailyHadith(): DailyHadithFetchingStatus {
        try {
            val idDateFormat = SimpleDateFormat("yyyyMMdd")

            if (!mDailyHadithDao.isTodaysHadithLoaded(idDateFormat.format(Date()).toLong())) {
                val rssParser = RssParser()
                val items =
                    rssParser.getRssChannel(THE_SUNNAH_REVIVAL_RSS_FEED).items
                val simpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
                val dailyHadithList =
                    items.filter { it.title != null && it.content != null && it.pubDate != null }
                        .map {
                            val publishDate = simpleDateFormat.parse(it.pubDate!!) ?: Date()
                            DailyHadith(
                                id = idDateFormat.format(publishDate).toLong(),
                                title = it.title!!,
                                pubDateMilliseconds = publishDate.time,
                                content = it.content!!
                            )
                        }
                mDailyHadithDao.insertDailyHadithList(dailyHadithList)
            }

            return DailyHadithFetchingStatus.SUCCESSFUL
        } catch (exception: Exception) {
            exception.printStackTrace()
            return DailyHadithFetchingStatus.FAILED
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DailyHadithRepository? = null

        @JvmStatic
        fun getInstance(context: Context): DailyHadithRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRepository(context).also { INSTANCE = it }
            }

        private fun buildRepository(context: Context) =
            DailyHadithRepository(context.applicationContext)
    }

    enum class DailyHadithFetchingStatus {
        LOADING, SUCCESSFUL, FAILED
    }
}