package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService
import com.thesunnahrevival.sunnahassistant.utilities.getMonthNumber
import com.thesunnahrevival.sunnahassistant.utilities.sunnahReminders
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)
    var selectedReminder: Reminder? = null
    var settingsValue: AppSettings? = null
    val messages = MutableLiveData<String>()

    fun addInitialReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.addInitialReminders()
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteReminder(reminder)
        }
    }

    fun insert(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.addReminder(reminder)
            if (reminder.isEnabled)
                startServiceFromCoroutine()
        }
    }

    suspend fun getNextScheduledReminderToday(offsetFromMidnight: Long, day: Int, month: Int, year: Int): Reminder? {
        return mRepository.getNextScheduledReminderToday(offsetFromMidnight, day, month, year)
    }

    suspend fun getNextScheduledReminderTomorrow(day: Int, month: Int, year: Int): Reminder? {
        return mRepository.getNextScheduledReminderTomorrow(day, month, year)
    }

    fun getStatusOfAddingListOfReminders() = mRepository.statusOfAddingListOfReminders

    fun getReminders(filter: Int): LiveData<List<Reminder>> {
        return when (filter) {
            1 -> mRepository.getPastReminders()
            2 -> mRepository.getRemindersOnDay(false)
            3 -> mRepository.getRemindersOnDay(true)
            4 -> mRepository.getPrayerTimes()
            5 -> mRepository.getWeeklyReminders()
            6 -> mRepository.getMonthlyReminders()
            7 -> mRepository.getOneTimeReminders()
            else -> mRepository.getUpcomingReminders()
        }
    }

    fun updatePrayerTimeDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updatePrayerDetails(oldPrayerDetails, newPrayerDetails)
        }
    }

    fun getSettings() = mRepository.appSettings

    suspend fun getAppSettingsValue(): AppSettings? {
        return mRepository.getAppSettingsValue()
    }

    fun updateGeneratedPrayerTimes(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isLoadFreshData(settings.month)) {
                if (settings.isAutomatic) {
                    mRepository.updateGeneratedPrayerTimes(
                            settings.latitude, settings.longitude,
                            settings.calculationMethod, settings.asrCalculationMethod,
                            settings.latitudeAdjustmentMethod)
                    //Save the Month in User Settings to prevent re-fetching the data the current month
                    settings.month = getMonthNumber(System.currentTimeMillis())
                    updateSettings(settings)
                }
            }
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updateAppSettings(settings)
        }
    }

    private fun isLoadFreshData(month: Int) = month != getMonthNumber(System.currentTimeMillis())

    val isDeviceOffline: Boolean
        get() {
            val connectivityManager = getApplication<Application>().applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo == null ||
                    !connectivityManager.activeNetworkInfo.isConnected
        }

    fun getGeocodingData(address: String) {
        val unavailableLocationString = getApplication<Application>().getString(R.string.unavailable_location)
        val serverError: String = getApplication<Application>().getString(R.string.server_error_occured)
        val noNetworkString = getApplication<Application>().getString(R.string.error_updating_location)

        viewModelScope.launch(Dispatchers.IO) {
            val data = mRepository.getGeocodingData(address, settingsValue?.language ?: "en")
            val message: String

            when {
                data != null && data.results.isNotEmpty() -> {
                    updateLocationDetails(data)
                    message = "Successful"
                }
                data != null && data.results.isEmpty() -> {
                    if (data.status == "ZERO_RESULTS")
                        message = unavailableLocationString
                    else {
                        message = serverError
                        mRepository.reportGeocodingServerError(data.status)
                    }

                }
                else -> {
                    message = noNetworkString
                }
            }

            withContext(Dispatchers.Main) {
               messages.value = message
            }
        }

    }

    private fun updateLocationDetails(data: GeocodingData) {
        val tempSettings = settingsValue
        val result = data.results[0]

        if (tempSettings != null) {
            tempSettings.formattedAddress = result.formattedAddress
            tempSettings.latitude = result.geometry.location.lat
            tempSettings.longitude = result.geometry.location.lng
            updateSettings(tempSettings)
        }
    }

    fun updatePrayerTimesData() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = settingsValue
            if (settings?.formattedAddress?.isNotBlank() == true) {
                mRepository.deletePrayerTimesData()
                if (settings.isAutomatic) {
                    mRepository.generatePrayerTimes(
                            settings.latitude, settings.longitude, settings.calculationMethod, settings.asrCalculationMethod,
                            settings.latitudeAdjustmentMethod
                    )
                }
                updateSettings(settings)
            }
        }
    }

    fun updatedDeletedCategories(deletedCategories: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (deletedCategories.isNotEmpty()) {
                mRepository.updateDeletedCategories(deletedCategories)
            }
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminder.isEnabled = true
            mRepository.setReminderIsEnabled(reminder)
            startServiceFromCoroutine()
        }
    }

    fun cancelScheduledReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminder.isEnabled = false
            mRepository.setReminderIsEnabled(reminder)
            startServiceFromCoroutine()
        }

    }

    private suspend fun startServiceFromCoroutine() {
        withContext(Dispatchers.Main) {
            getApplication<Application>().startService(Intent(getApplication(), NextReminderService::class.java))
        }
    }

    fun localeUpdate() {
        if (supportedLocales.contains(Locale.getDefault().language)) {
            val  configuration = Configuration(getApplication<Application>().applicationContext.resources.configuration)
            configuration.setLocale(Locale(settingsValue?.language ?: "en"))

            val oldCategoryNames = getApplication<Application>().applicationContext
                    .createConfigurationContext(configuration)
                    .resources
                    .getStringArray(R.array.categories)

            val newCategoryNames = mutableListOf<String>()
            newCategoryNames.addAll(( getApplication<Application>().resources.getStringArray(R.array.categories)))
            val reminders = sunnahReminders(getApplication())

            viewModelScope.launch(Dispatchers.IO){
                for ((index, oldCategoryName) in oldCategoryNames.withIndex()){
                    mRepository.updateCategory(oldCategoryName, newCategoryNames[index])
                }
                for (reminder in reminders){
                    mRepository.updateReminder(reminder.id, reminder.reminderName, reminder.reminderInfo, reminder.category)
                }
                updatePrayerTimesData()
                settingsValue?.language = Locale.getDefault().language
                settingsValue?.categories?.removeAll(oldCategoryNames)
                settingsValue?.categories?.addAll(newCategoryNames)
                settingsValue?.let {
                    updateSettings(it)
                }

            }
        }
    }

}