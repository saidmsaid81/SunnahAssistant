package com.thesunnahrevival.common.data

import android.content.Context
import androidx.paging.PagingSource
import com.thesunnahrevival.common.ApiKey
import com.thesunnahrevival.common.data.local.ReminderDao
import com.thesunnahrevival.common.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.GeocodingData
import com.thesunnahrevival.common.data.model.PrayerTimeCalculator
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.data.remote.GeocodingInterface
import com.thesunnahrevival.common.data.remote.SunnahAssistantApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class SunnahAssistantRepository private constructor(context: Context) {
    private val mReminderDao: ReminderDao =
        SunnahAssistantDatabase.getInstance(context).reminderDao()
    private val mGeocodingRestApi: GeocodingInterface
    private val mSunnahAssistantApi: SunnahAssistantApiInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mGeocodingRestApi = retrofit.create(GeocodingInterface::class.java)

        mSunnahAssistantApi = Retrofit.Builder()
            .baseUrl("https://us-central1-sunnah-assistant.cloudfunctions.net/")
            .build()
            .create(SunnahAssistantApiInterface::class.java)

    }

    suspend fun insertReminder(reminder: Reminder) = mReminderDao.insertReminder(reminder)

    //TODO use insert reminder
    suspend fun updateReminder(id: Int, name: String?, info: String?, category: String?) {
        if (name != null && info != null && category != null)
            mReminderDao.updateReminder(id, name, info, category)
    }

    suspend fun deleteReminder(reminder: Reminder) = mReminderDao.deleteReminder(reminder)

    fun thereRemindersOnDay(
        excludeCategory: String,
        dayOfWeek: String,
        dayOfMonth: Int,
        month: Int,
        year: Int
    ): Boolean {
        return mReminderDao.thereRemindersOnDay(
            excludeCategory, dayOfWeek, dayOfMonth, month, year
        )
    }

    fun getRemindersOnDay(date: Date, category: String): PagingSource<Int, Reminder> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return mReminderDao.getRemindersOnDay(
            calendar.get(Calendar.DAY_OF_WEEK).toString(),
            day,
            month,
            year,
            category
        )
    }

    fun getRemindersOnDayValue(numberOfTheWeekDay: String, day: Int, month: Int, year: Int) =
        mReminderDao.getRemindersOnDayValue(numberOfTheWeekDay, day, month, year)

    suspend fun getNextTimeForReminderToday(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mReminderDao.getNextTimeForReminderToday(
        offsetFromMidnight,
        numberOfTheWeekDay,
        day,
        month,
        year
    )

    suspend fun getNextTimeForReminderTomorrow(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mReminderDao.getNextTimeForReminderTomorrow(
        offsetFromMidnight,
        numberOfTheWeekDay,
        day,
        month,
        year
    )

    suspend fun getNextScheduledReminderToday(
        timeForReminder: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mReminderDao.getNextScheduledReminderToday(
        timeForReminder,
        numberOfTheWeekDay,
        day,
        month,
        year
    )

    suspend fun getNextScheduledReminderTomorrow(
        timeForReminder: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mReminderDao.getNextScheduledReminderTomorrow(
        timeForReminder,
        numberOfTheWeekDay,
        day,
        month,
        year
    )

    suspend fun updatePrayerDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        mReminderDao.updatePrayerTimeDetails(
            oldPrayerDetails.reminderName,
            newPrayerDetails.reminderName,
            newPrayerDetails.reminderInfo,
            newPrayerDetails.offsetInMinutes,
            newPrayerDetails.isEnabled
        )
    }

    suspend fun deletePrayerTimesData() =
        mReminderDao.deleteAllPrayerTimes()

    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String) =
        mReminderDao.getPrayerTimesValue(day, month, year, categoryName)

    fun generatePrayerTimes(
        date: Date,
        prayerNames: Array<String>,
        prayerCategory: String
    ) {

        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val settings = mReminderDao.getAppSettingsValue()

        if (settings != null && settings.isAutomatic) {
            val therePrayerRemindersOnDay =
                mReminderDao.therePrayerRemindersOnDay(prayerCategory, "$day$month$year")
            if (!therePrayerRemindersOnDay) {
                val prayerTimeCalculator = PrayerTimeCalculator(
                    settings.latitude.toDouble(),
                    settings.longitude.toDouble(),
                    settings.calculationMethod,
                    settings.asrCalculationMethod,
                    settings.latitudeAdjustmentMethod,
                    prayerNames,
                    prayerCategory
                )

                val prayerTimeReminders =
                    prayerTimeCalculator.getPrayerTimeReminders(day, month, year)

                mReminderDao.insertRemindersList(prayerTimeReminders)
            }
        }
    }

    suspend fun updateCategory(deletedCategory: String, newCategory: String) {
        mReminderDao.updateCategory(deletedCategory, newCategory)
    }

    suspend fun updateDeletedCategories(
        deletedCategories: ArrayList<String>,
        uncategorized: String
    ) {
        for (deletedCategory in deletedCategories) {
            mReminderDao.updateCategory(
                deletedCategory,
                uncategorized
            )
        }
    }


    suspend fun updateAppSettings(settings: AppSettings) = mReminderDao.updateAppSettings(settings)

    fun getAppSettings() = mReminderDao.getAppSettings()

    suspend fun getAppSettingsValue(): AppSettings? {
        return mReminderDao.getAppSettingsValue()
    }

    suspend fun updateWidgetSettings(
        isShowHijriDateWidget: Boolean,
        isDisplayNextReminder: Boolean
    ) =
        mReminderDao.updateWidgetSettings(isShowHijriDateWidget, isDisplayNextReminder)

    suspend fun getGeocodingData(address: String, locale: String): GeocodingData? {
        return mGeocodingRestApi.getGeocodingData(address, ApiKey.API_KEY, locale)
    }

    suspend fun reportGeocodingServerError(status: String) {
        mSunnahAssistantApi.reportGeocodingError(status)
    }

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantRepository? = null

        @JvmStatic
        fun getInstance(context: Context): SunnahAssistantRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRepository(context).also { INSTANCE = it }
            }

        private fun buildRepository(context: Context) =
            SunnahAssistantRepository(context)
    }
}