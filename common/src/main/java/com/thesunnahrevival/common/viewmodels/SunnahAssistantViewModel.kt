package com.thesunnahrevival.common.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.lifecycle.*
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.data.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.GeocodingData
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.utilities.NextReminderService
import com.thesunnahrevival.common.utilities.getMonthNumber
import com.thesunnahrevival.common.utilities.sunnahReminders
import com.thesunnahrevival.common.utilities.supportedLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Frequency

class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)

    var selectedReminder: Reminder = Reminder(
        reminderName = "", frequency = Frequency.OneTime,
        category = application.resources.getStringArray(R.array.categories)[0] //Uncategorized
    )

    var settingsValue: AppSettings? = null
    val messages = MutableLiveData<String>()
    var isPrayerSettingsUpdated = false
    private val mutableDateOfReminders = MutableLiveData<Long>()
    val triggerCalendarUpdate = MutableLiveData<Boolean>()

    fun addInitialReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.addInitialReminders()
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteReminder(reminder)
            startServiceFromCoroutine()
            withContext(Dispatchers.Main) {
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.addReminder(reminder)
            if (reminder.isEnabled)
                startServiceFromCoroutine()
            withContext(Dispatchers.Main) {
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun getStatusOfAddingListOfReminders() = mRepository.statusOfAddingListOfReminders

    fun setDateOfReminders(date: Long) {
        mutableDateOfReminders.value = date
    }

    fun getReminders(): LiveData<List<Reminder>> {
        return Transformations.switchMap(mutableDateOfReminders) { dateOfReminders ->
            mRepository.getRemindersOnDay(Date(dateOfReminders))
        }
    }

    fun thereRemindersOnDay(dayOfWeek: String, dayOfMonth: Int, month: Int, year: Int): Boolean {
        val excludePrayer = getApplication<Application>().getString(R.string.prayer)
        return mRepository.thereRemindersOnDay(excludePrayer, dayOfWeek, dayOfMonth, month, year)
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
                    !connectivityManager.activeNetworkInfo!!.isConnected
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
            isPrayerSettingsUpdated = true
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