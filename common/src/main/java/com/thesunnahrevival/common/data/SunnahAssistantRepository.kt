package com.thesunnahrevival.common.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.common.ApiKey
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.local.ReminderDao
import com.thesunnahrevival.common.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.GeocodingData
import com.thesunnahrevival.common.data.model.PrayerTimeCalculator
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.data.remote.GeocodingInterface
import com.thesunnahrevival.common.data.remote.SunnahAssistantApiInterface
import com.thesunnahrevival.common.utilities.getMonthNumber
import com.thesunnahrevival.common.utilities.getYear
import com.thesunnahrevival.common.utilities.sunnahReminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class SunnahAssistantRepository private constructor(context: Context) {
    private val mReminderDao: ReminderDao =
        SunnahAssistantDatabase.getInstance(context).reminderDao()
    private val mGeocodingRestApi: GeocodingInterface
    private val mSunnahAssistantApi: SunnahAssistantApiInterface
    val statusOfAddingListOfReminders = MutableLiveData<Boolean>()

    val appSettings: LiveData<AppSettings?>
        get() = mReminderDao.getAppSettings()

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

    suspend fun addReminder(reminder: Reminder) = mReminderDao.insertReminder(reminder)

    suspend fun updatePrayerDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        mReminderDao.updatePrayerTimeDetails(
            oldPrayerDetails.reminderName,
            newPrayerDetails.reminderName,
            newPrayerDetails.reminderInfo,
            newPrayerDetails.offsetInMinutes,
            newPrayerDetails.isEnabled
        )

    }

    suspend fun deleteReminder(reminder: Reminder) = mReminderDao.deleteReminder(reminder)

    suspend fun deletePrayerTimesData(prayerCategory: String) =
        mReminderDao.deleteAllPrayerTimes(prayerCategory)

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

    fun getRemindersOnDayValue(numberOfTheWeekDay: String, day: Int, month: Int, year: Int) =
        mReminderDao.getRemindersOnDayValue(numberOfTheWeekDay, day, month, year)

    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String) =
        mReminderDao.getPrayerTimesValue(day, month, year, categoryName)

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

    suspend fun updateWidgetSettings(
        isShowHijriDateWidget: Boolean,
        isDisplayNextReminder: Boolean
    ) =
        mReminderDao.updateWidgetSettings(isShowHijriDateWidget, isDisplayNextReminder)

    fun getRemindersOnDay(date: Date): LiveData<List<Reminder>> {
        val calendar = Calendar.getInstance()
        calendar.time = date

        return mReminderDao.getRemindersOnDay(
            calendar.get(Calendar.DAY_OF_WEEK).toString(),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )
    }

    suspend fun addInitialReminders(context: Context) {
        updateStatusOfAddingLists(false)
        mReminderDao.addRemindersList(sunnahReminders(context))
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                context.getString(R.string.successfuly_added_sunnah_reminders),
                Toast.LENGTH_LONG
            ).show()
        }
        updateStatusOfAddingLists(true)
    }

    suspend fun updateAppSettings(settings: AppSettings) = mReminderDao.updateAppSettings(settings)

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

    suspend fun updateReminder(id: Int, name: String?, info: String?, category: String?) {
        if (name != null && info != null && category != null)
            mReminderDao.updateReminder(id, name, info, category)
    }

    suspend fun updateCategory(deletedCategory: String, newCategory: String) {
        mReminderDao.updateCategory(deletedCategory, newCategory)
    }

    suspend fun generatePrayerTimes(
        latitude: Float,
        longitude: Float,
        method: CalculationMethod,
        asrCalculationMethod: Madhab,
        latitudeAdjustmentMethod: Int,
        prayerNames: Array<String>,
        prayerCategory: String
    ) {
        updateStatusOfAddingLists(false)
        val prayerTimesReminders = PrayerTimeCalculator(
            latitude.toDouble(),
            longitude.toDouble(),
            method,
            asrCalculationMethod,
            latitudeAdjustmentMethod,
            prayerNames,
            prayerCategory
        )
            .getPrayerTimeReminders()
        mReminderDao.addRemindersList(prayerTimesReminders)
        updateStatusOfAddingLists(true)
    }

    private suspend fun updateStatusOfAddingLists(status: Boolean) {
        withContext(Dispatchers.Main) {
            statusOfAddingListOfReminders.value = status
        }
    }

    suspend fun updateGeneratedPrayerTimes(
        latitude: Float,
        longitude: Float,
        method: CalculationMethod,
        asrCalculationMethod: Madhab,
        latitudeAdjustmentMethod: Int,
        prayerNames: Array<String>,
        prayerCategory: String
    ) {
        updateStatusOfAddingLists(false)
        val prayerTimesReminders = PrayerTimeCalculator(
            latitude.toDouble(), longitude.toDouble(),
            method, asrCalculationMethod, latitudeAdjustmentMethod,
            prayerNames,
            prayerCategory
        )
            .getPrayerTimeReminders()
        for (prayerTimeReminder in prayerTimesReminders) {
            mReminderDao.updateGeneratedPrayerTimes(
                prayerTimeReminder.id,
                getMonthNumber(System.currentTimeMillis()),
                getYear(System.currentTimeMillis()).toInt(),
                prayerTimeReminder.timeInSeconds
            )
        }
        updateStatusOfAddingLists(true)

    }

    suspend fun getGeocodingData(address: String, locale: String): GeocodingData? {
        return mGeocodingRestApi.getGeocodingData(address, ApiKey.API_KEY, locale)
    }

    suspend fun getAppSettingsValue(): AppSettings? {
        return mReminderDao.getAppSettingsValue()
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