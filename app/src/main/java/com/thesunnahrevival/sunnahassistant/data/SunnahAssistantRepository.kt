package com.thesunnahrevival.sunnahassistant.data

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.data.remote.AladhanRestApi
import com.thesunnahrevival.sunnahassistant.data.remote.GeocodingRestApi
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SunnahAssistantRepository private constructor(val application: Application){
    private val mReminderDAO: ReminderDAO = SunnahAssistantDatabase.getInstance(application).reminderDao()
    private val mGeocodingRestApi = GeocodingRestApi.getInstance()
    private var mDay = TimeDateUtil.getDayDate(System.currentTimeMillis())

    val geocodingData: LiveData<GeocodingData>
        get() = mGeocodingRestApi.data
    val appSettings: LiveData<AppSettings>
        get() = mReminderDAO.appSettings
    val errorMessages: LiveData<String>
        get() = AladhanRestApi.errorMessages

    companion object {

        @Volatile private var INSTANCE: SunnahAssistantRepository? = null

        @JvmStatic
        fun getInstance(application: Application): SunnahAssistantRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildRepository(application).also { INSTANCE = it }
                }

        private fun buildRepository(application: Application) = SunnahAssistantRepository(application)
    }

    fun addReminder(reminder: Reminder) {
        CoroutineScope(Dispatchers.IO).launch {
            mReminderDAO.insertReminder(reminder)
        }
    }

    fun updatePrayerDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        CoroutineScope(Dispatchers.IO).launch {
            mReminderDAO.updatePrayerTimeDetails(oldPrayerDetails.reminderName, newPrayerDetails.reminderName, newPrayerDetails.reminderInfo, newPrayerDetails.offset )
        }
    }

    fun deleteReminder(reminder: Reminder) {
       CoroutineScope(Dispatchers.IO).launch {
           mReminderDAO.deleteReminder(reminder)
       }
    }

    fun deletePrayerTimesData() {
        CoroutineScope(Dispatchers.IO).launch {
            mReminderDAO.deleteAllPrayerTimes()
        }
    }

    fun setReminderIsEnabled(reminder: Reminder) {
        CoroutineScope(Dispatchers.IO).launch {
            if (reminder.category.matches(SunnahAssistantUtil.PRAYER.toRegex()))
                mReminderDAO.setPrayerTimeEnabled(reminder.reminderName, reminder.isEnabled)
            else
                mReminderDAO.setEnabled(reminder.id, reminder.isEnabled)
        }

    }

    fun getAllReminders(filter: Int, nameOfTheDay: String): LiveData<List<Reminder>> {
        return when (filter) {
            1 -> mReminderDAO.getPastReminders(TimeDateUtil.calculateOffsetFromMidnight(), nameOfTheDay,
                        mDay, TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1,
                        TimeDateUtil.getYear(System.currentTimeMillis()).toInt())
            2 -> mReminderDAO.getRemindersOnDay(TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()),
                    mDay, TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1,
                    TimeDateUtil.getYear(System.currentTimeMillis()).toInt())
            3 -> mReminderDAO.getRemindersOnDay(
                    TimeDateUtil.getNameOfTheDay(System.currentTimeMillis() + 86400000), mDay + 1, TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1, TimeDateUtil.getYear(System.currentTimeMillis()).toInt())
            4 -> mReminderDAO.getPrayerTimes(mDay)
            5 -> mReminderDAO.weeklyReminders
            6 -> mReminderDAO.monthlyReminder
            7 -> mReminderDAO.oneTimeReminders
            else -> mReminderDAO.getUpcomingReminders(TimeDateUtil.calculateOffsetFromMidnight(), nameOfTheDay,
                    mDay, TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1,
                    TimeDateUtil.getYear(System.currentTimeMillis()).toInt())
        }
    }

    fun addInitialReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            val messages = mReminderDAO.addRemindersListIfNotExists(SunnahAssistantUtil.sunnahReminders())
            withContext(Dispatchers.Main){
                Toast.makeText(application, messages, Toast.LENGTH_LONG).show()
            }
        }
    }



    fun updateAppSettings(settings: AppSettings) {
        CoroutineScope(Dispatchers.IO).launch {
            mReminderDAO.updateAppSettings(settings)
        }

    }

    fun updateDeletedCategories(deletedCategories: ArrayList<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            for (deletedCategory in deletedCategories) {
                mReminderDAO.updateCategory(deletedCategory, SunnahAssistantUtil.UNCATEGORIZED)
            }
        }
    }

    fun updateNotificationSettings(notificationToneUri: Uri?, isVibrate: Boolean, priority: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            mReminderDAO.updateNotificationSettings(notificationToneUri, isVibrate, priority)
        }
    }

    fun fetchPrayerTimes(latitude: Float, longitude: Float, month: String?, year: String?, method: Int, asrCalculationMethod: Int, latitudeAdjustmentMethod: Int) {
        AladhanRestApi.fetchPrayerTimes(mReminderDAO, latitude, longitude, month, year, method, asrCalculationMethod, latitudeAdjustmentMethod)
    }


    fun fetchGeocodingData(address: String?) {
        mGeocodingRestApi.fetchGeocodingData(address)
    }

}