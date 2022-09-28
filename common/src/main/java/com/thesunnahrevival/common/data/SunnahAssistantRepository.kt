package com.thesunnahrevival.common.data

import android.content.Context
import androidx.paging.PagingSource
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.common.ApiKey
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.local.ReminderDao
import com.thesunnahrevival.common.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.common.data.model.*
import com.thesunnahrevival.common.data.remote.GeocodingInterface
import com.thesunnahrevival.common.data.remote.SunnahAssistantApiInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.*

class SunnahAssistantRepository private constructor(context: Context) {
    private val mReminderDao: ReminderDao =
        SunnahAssistantDatabase.getInstance(context).reminderDao()
    private val mGeocodingRestApi: GeocodingInterface
    private val mSunnahAssistantApi: SunnahAssistantApiInterface
    private val prayerNames: Array<String>
    private val prayerCategory: String

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

        prayerNames = context.resources.getStringArray(R.array.prayer_names)
        prayerCategory = context.resources.getStringArray(R.array.categories)[2]

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
        CoroutineScope(Dispatchers.IO).launch {
            generatePrayerTimes(date)
        }

        val (day, month, year, dayOfWeek) = getReminderDate(date)
        return mReminderDao.getRemindersOnDay(
            dayOfWeek.toString(),
            day,
            month,
            year,
            category
        )
    }

    fun getRemindersOnDayValue(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<Reminder> {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mReminderDao.getRemindersOnDayValue(numberOfTheWeekDay, day, month, year)
    }

    suspend fun getNextTimeForReminderForDay(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Long? {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mReminderDao.getNextTimeForReminderForDay(
            offsetFromMidnight,
            numberOfTheWeekDay,
            day,
            month,
            year
        )
    }

    suspend fun getNextScheduledRemindersForDay(
        timeForReminder: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mReminderDao.getNextScheduledRemindersForDay(
        timeForReminder,
        numberOfTheWeekDay,
        day,
        month,
        year
    )


    suspend fun updatePrayerDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        mReminderDao.updatePrayerTimeDetails(
            newPrayerDetails.reminderInfo,
            newPrayerDetails.offsetInMinutes,
            newPrayerDetails.isEnabled,
            newPrayerDetails.isComplete,
            oldPrayerDetails.id
        )
    }

    suspend fun deletePrayerTimesData() =
        mReminderDao.deleteAllPrayerTimes()

    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String): List<Reminder> {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mReminderDao.getPrayerTimesValue(day, month, year, categoryName)
    }

    private fun generatePrayerTimes(date: Date) {
        val settings = mReminderDao.getAppSettingsValue()
        if (settings != null && settings.isAutomaticPrayerAlertsEnabled && settings.formattedAddress?.isNotBlank() == true) {
            val (day, month, year) = getReminderDate(date)
            val therePrayerRemindersOnDay =
                mReminderDao.therePrayerRemindersOnDay(prayerCategory, "$day$month$year")
            if (!therePrayerRemindersOnDay && date.after(settings.generatePrayerRemindersAfter)) {
                val prayerTimeReminders =
                    getPrayerRemindersList(
                        day,
                        month,
                        year,
                        settings.latitude,
                        settings.longitude,
                        settings.calculationMethod,
                        settings.asrCalculationMethod,
                        settings.latitudeAdjustmentMethod,
                        prayerNames,
                        prayerCategory,
                        settings.generatePrayerTimeForPrayer,
                        settings.prayerTimeOffsetsInMinutes
                    )
                mReminderDao.insertRemindersList(prayerTimeReminders)
            }
        }
    }

    suspend fun updatePrayerReminders(prayerNames: Array<String>, prayerCategory: String) {
        val settings = mReminderDao.getAppSettingsValue()
        if (settings != null && settings.isAutomaticPrayerAlertsEnabled) {
            val todayDate = LocalDate.now()
            val upcomingPrayerDatesList = mReminderDao.getUpcomingPrayerDates(
                todayDate.dayOfMonth, todayDate.month.ordinal, todayDate.year
            )

            for (upcomingPrayerDate in upcomingPrayerDatesList) {
                val prayerRemindersList = getPrayerRemindersList(
                    upcomingPrayerDate.day,
                    upcomingPrayerDate.month,
                    upcomingPrayerDate.year,
                    settings.latitude,
                    settings.longitude,
                    settings.calculationMethod,
                    settings.asrCalculationMethod,
                    settings.latitudeAdjustmentMethod,
                    prayerNames,
                    prayerCategory,
                    settings.generatePrayerTimeForPrayer,
                    settings.prayerTimeOffsetsInMinutes
                )
                for (prayerReminder in prayerRemindersList) {
                    mReminderDao.updateGeneratedPrayerTime(
                        prayerReminder.id,
                        prayerReminder.timeInSeconds,
                        prayerReminder.isEnabled,
                        prayerReminder.offsetInMinutes
                    )
                }
            }
        }
    }

    suspend fun updatePrayerNames(oldPrayerNames: Array<String>, newPrayerNames: Array<String>) {
        if (oldPrayerNames.size == newPrayerNames.size) {
            oldPrayerNames.forEachIndexed { index, oldPrayerName ->
                mReminderDao.updatePrayerNames(oldPrayerName, newPrayerNames[index])
            }
        }
    }

    private fun getPrayerRemindersList(
        day: Int,
        month: Int,
        year: Int,
        latitude: Float,
        longitude: Float,
        calculationMethod: CalculationMethod,
        asrCalculationMethod: Madhab,
        latitudeAdjustmentMethod: Int,
        prayerNames: Array<String>,
        prayerCategory: String,
        generatePrayerTimeForPrayer: BooleanArray,
        offsetInMinutesForPrayer: IntArray
    ): ArrayList<Reminder> {

        val prayerTimeCalculator = PrayerTimeCalculator(
            latitude.toDouble(),
            longitude.toDouble(),
            calculationMethod,
            asrCalculationMethod,
            latitudeAdjustmentMethod,
            prayerNames,
            prayerCategory
        )
        return prayerTimeCalculator.getPrayerTimeReminders(
            day,
            month,
            year,
            generatePrayerTimeForPrayer,
            offsetInMinutesForPrayer
        )
    }

    private fun getReminderDate(date: Date): ReminderDate {
        val calendar = GregorianCalendar()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).toString()
        return ReminderDate(day, month, year, dayOfWeek)
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