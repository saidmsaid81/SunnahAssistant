package com.thesunnahrevival.sunnahassistant.data

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.thesunnahrevival.sunnahassistant.ApiKey
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.ReminderDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimeCalculator
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.data.remote.GeocodingInterface
import com.thesunnahrevival.sunnahassistant.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class SunnahAssistantRepository private constructor(private val application: Application){
    private val mReminderDao: ReminderDao = SunnahAssistantDatabase.getInstance(application).reminderDao()
    private val mGeocodingRestApi: GeocodingInterface
    private var mDay = getDayDate(System.currentTimeMillis())

    val appSettings: LiveData<AppSettings?>
        get() = mReminderDao.getAppSettings()

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        mGeocodingRestApi = retrofit.create(GeocodingInterface::class.java)
    }

    suspend fun addReminder(reminder: Reminder) = mReminderDao.insertReminder(reminder)

    suspend fun updatePrayerDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        mReminderDao.updatePrayerTimeDetails(oldPrayerDetails.reminderName, newPrayerDetails.reminderName,
                newPrayerDetails.reminderInfo, newPrayerDetails.offset )

    }

    suspend fun deleteReminder(reminder: Reminder) = mReminderDao.deleteReminder(reminder)

    suspend fun deletePrayerTimesData() = mReminderDao.deleteAllPrayerTimes(application.resources.getStringArray(R.array.categories)[2])

    suspend fun setReminderIsEnabled(reminder: Reminder) {
        val prayer = application.resources.getStringArray(R.array.categories)[2]
        if (reminder.category?.matches(prayer.toRegex()) == true)
            reminder.reminderName?.let { mReminderDao.setPrayerTimeEnabled(it, reminder.isEnabled) }
        else
            mReminderDao.setEnabled(reminder.id, reminder.isEnabled)
    }

    suspend fun getNextScheduledReminderToday(offsetFromMidnight: Long, day: Int, month: Int, year: Int): Reminder? {
       
        return mReminderDao.getNextScheduledReminderToday(offsetFromMidnight, dayOfTheWeek.toString(), day, month, year)
    }

    suspend fun getNextScheduledReminderTomorrow(day: Int, month: Int, year: Int): Reminder? {

        return mReminderDao.getNextScheduledReminderTomorrow(dayOfTheWeek.toString(), day, month, year)
    }

    fun getUpcomingReminders(): LiveData<List<Reminder>> {
        
        return mReminderDao.getUpcomingReminders(calculateOffsetFromMidnight(),  dayOfTheWeek.toString() ,
                mDay, getMonthNumber(System.currentTimeMillis()),
                getYear(System.currentTimeMillis()).toInt())
    }

    fun getOneTimeReminders() = mReminderDao.getOneTimeReminders()

    fun getMonthlyReminders() = mReminderDao.getMonthlyReminder()

    fun getWeeklyReminders() = mReminderDao.getWeeklyReminders()

    fun getPrayerTimes(): LiveData<List<Reminder>> {
        return mReminderDao.getPrayerTimes(mDay,
                getMonthNumber(System.currentTimeMillis()),
                getYear(System.currentTimeMillis()).toInt(), application.resources.getStringArray(R.array.categories)[2])
    }

    fun getRemindersOnDay(day: Int): LiveData<List<Reminder>> {
        
        return mReminderDao.getRemindersOnDay(
                dayOfTheWeek.toString(), day, getMonthNumber(System.currentTimeMillis()), getYear(System.currentTimeMillis()).toInt())
    }

    fun getPastReminders(): LiveData<List<Reminder>> {
        
        return mReminderDao.getPastReminders(calculateOffsetFromMidnight(), dayOfTheWeek.toString(),
                mDay, getMonthNumber(System.currentTimeMillis()),
                getYear(System.currentTimeMillis()).toInt())
    }

    suspend fun addInitialReminders() {
        mReminderDao.addRemindersList(sunnahReminders(application))
        withContext(Dispatchers.Main){
            Toast.makeText(application, application.getString(R.string.successfuly_added_sunnah_reminders), Toast.LENGTH_LONG).show()
        }
    }

    suspend fun updateAppSettings(settings: AppSettings) = mReminderDao.updateAppSettings(settings)

    suspend fun updateDeletedCategories(deletedCategories: ArrayList<String>) {
        for (deletedCategory in deletedCategories) {
            mReminderDao.updateCategory(deletedCategory, application.resources.getStringArray(R.array.categories)[0])
        }
    }

    suspend fun updateReminder(id: Int, name: String?, info: String?, category: String?){
        if (name != null && info != null && category != null)
            mReminderDao.updateReminder(id, name, info, category)
    }

    suspend fun updateCategory(deletedCategory: String, newCategory: String){
        mReminderDao.updateCategory(deletedCategory, newCategory)
    }

    suspend fun generatePrayerTimes(latitude: Float, longitude: Float, method: Int, asrCalculationMethod: Int, latitudeAdjustmentMethod: Int, locale: Locale) {
        val prayerTimesReminders = PrayerTimeCalculator(latitude.toDouble(), longitude.toDouble(),
                method, asrCalculationMethod, latitudeAdjustmentMethod, application.resources.getStringArray(R.array.prayer_names), application.resources.getStringArray(R.array.categories)[2])
                .getPrayerTimeReminders()
        mReminderDao.addRemindersList(prayerTimesReminders)
    }

    suspend fun updateGeneratedPrayerTimes(latitude: Float, longitude: Float, method: Int, asrCalculationMethod: Int, latitudeAdjustmentMethod: Int, locale: Locale) {
        val prayerTimesReminders = PrayerTimeCalculator(latitude.toDouble(), longitude.toDouble(),
                method, asrCalculationMethod, latitudeAdjustmentMethod,
                application.resources.getStringArray(R.array.prayer_names),
                application.resources.getStringArray(R.array.categories)[2])
                .getPrayerTimeReminders()
        for (prayerTimeReminder in prayerTimesReminders){
            mReminderDao.updateGeneratedPrayerTimes(prayerTimeReminder.id,
                    getMonthNumber(System.currentTimeMillis()),
                    getYear(System.currentTimeMillis()).toInt(),
                    prayerTimeReminder.timeInSeconds)
        }

    }


    suspend fun getGeocodingData(address: String, locale: String): GeocodingData? {
        return mGeocodingRestApi.getGeocodingData(address, ApiKey.API_KEY, locale)
    }

    suspend fun getAppSettingsValue(): AppSettings? {
        return mReminderDao.getAppSettingsValue()
    }

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantRepository? = null

        @JvmStatic
        fun getInstance(application: Application): SunnahAssistantRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildRepository(application).also { INSTANCE = it }
                }

        private fun buildRepository(application: Application) = SunnahAssistantRepository(application)
    }

}