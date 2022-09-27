package com.thesunnahrevival.common.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.data.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.GeocodingData
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.services.NextReminderService
import com.thesunnahrevival.common.utilities.getPackageNameToUse
import com.thesunnahrevival.common.utilities.sunnahReminders
import com.thesunnahrevival.common.utilities.supportedLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*


class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)

    var selectedReminder: Reminder = Reminder(
        reminderName = "", frequency = Frequency.OneTime,
        category = application.resources.getStringArray(R.array.categories)[0], //Uncategorized
        day = LocalDate.now().dayOfMonth,
        month = LocalDate.now().month.ordinal,
        year = LocalDate.now().year
    )

    var settingsValue: AppSettings? = null
    val messages = MutableLiveData<String>()
    var isPrayerSettingsUpdated = false
    private val mutableReminderParameters =
        MutableLiveData(Pair(System.currentTimeMillis(), ""))
    val triggerCalendarUpdate = MutableLiveData<Boolean>()
    private var browserPackageNameToUse: String? = null

    fun setReminderParameters(date: Long? = null, category: String? = null) {
        val currentDateParameter =
            mutableReminderParameters.value?.first ?: System.currentTimeMillis()
        val currentCategoryParameter = mutableReminderParameters.value?.second ?: ""
        mutableReminderParameters.value =
            Pair(date ?: currentDateParameter, category ?: currentCategoryParameter)
    }

    fun insertReminder(reminder: Reminder, updateCalendar: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = mRepository.insertReminder(reminder)
            reminder.id = id.toInt()
            selectedReminder = reminder
            withContext(Dispatchers.Main) {
                startService()
                if (updateCalendar)
                    triggerCalendarUpdate.value = true
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteReminder(reminder)
            withContext(Dispatchers.Main) {
                startService()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun thereRemindersOnDay(dayOfWeek: String, dayOfMonth: Int, month: Int, year: Int): Boolean {
        val excludePrayer = getContext().getString(R.string.prayer)
        return mRepository.thereRemindersOnDay(excludePrayer, dayOfWeek, dayOfMonth, month, year)
    }

    fun getReminders(): LiveData<PagingData<Reminder>> {
        return Transformations.switchMap(mutableReminderParameters) { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getRemindersOnDay(Date(dateOfReminders), category)
                }
            ).liveData
        }
    }

    fun updatePrayerTimeDetails(oldPrayerDetails: Reminder, newPrayerDetails: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updatePrayerDetails(oldPrayerDetails, newPrayerDetails)
            withContext(Dispatchers.Main) {
                startService()
            }
        }
    }

    fun updatePrayerTimesData() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = settingsValue
            if (settings?.formattedAddress?.isNotBlank() == true) {
                if (settings.isAutomaticPrayerAlertsEnabled) {
                    mRepository.updatePrayerReminders(
                        getContext().resources.getStringArray(R.array.prayer_names),
                        getContext().resources.getStringArray(R.array.categories)[2]
                    )
                } else
                    mRepository.deletePrayerTimesData()
                updateSettings(settings)
            }
        }
    }

    fun updatedDeletedCategories(deletedCategories: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (deletedCategories.isNotEmpty()) {
                mRepository.updateDeletedCategories(
                    deletedCategories,
                    getContext().resources.getStringArray(R.array.categories)[0]
                )
            }
        }
    }

    fun getSettings() = mRepository.getAppSettings().asLiveData()

    suspend fun getAppSettingsValue(): AppSettings? {
        return mRepository.getAppSettingsValue()
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updateAppSettings(settings)
        }
    }

    fun getGeocodingData(address: String) {
        val unavailableLocationString =
            getContext().getString(R.string.unavailable_location)
        val serverError: String =
            getContext().getString(R.string.server_error_occured)
        val noNetworkString =
            getContext().getString(R.string.error_updating_location)

        viewModelScope.launch(Dispatchers.IO) {
            val data = mRepository.getGeocodingData(address, settingsValue?.language ?: "en")
            val message: String

            when {
                data != null && data.results.isNotEmpty() -> {
                    updateLocationDetails(data)
                    message = "Successful"
                }
                data != null -> {
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

    private fun startService() {
        getApplication<Application>().startService(
            Intent(
                getApplication(),
                NextReminderService::class.java
            )
        )
    }

    private fun getContext() = getApplication<Application>().applicationContext

    val isDeviceOffline: Boolean
        get() {
            val connectivityManager = getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo == null ||
                    !connectivityManager.activeNetworkInfo!!.isConnected
        }

    fun localeUpdate() {
        if (supportedLocales.contains(Locale.getDefault().language)) {
            val configuration =
                Configuration(getContext().resources.configuration)
            configuration.setLocale(Locale(settingsValue?.language ?: "en"))

            val oldCategoryNames = getContext()
                .createConfigurationContext(configuration)
                .resources
                .getStringArray(R.array.categories)

            val newCategoryNames = mutableListOf<String>()
            newCategoryNames.addAll((getContext().resources.getStringArray(R.array.categories)))

            val oldPrayerNames = getContext()
                .createConfigurationContext(configuration)
                .resources
                .getStringArray(R.array.prayer_names)
            val newPrayerNames = getContext().resources.getStringArray(R.array.prayer_names)

            val reminders = sunnahReminders(getApplication())

            viewModelScope.launch(Dispatchers.IO) {
                for ((index, oldCategoryName) in oldCategoryNames.withIndex()) {
                    mRepository.updateCategory(oldCategoryName, newCategoryNames[index])
                }
                for (reminder in reminders) {
                    mRepository.updateReminder(
                        reminder.id,
                        reminder.reminderName,
                        reminder.reminderInfo,
                        reminder.category
                    )
                }

                mRepository.updatePrayerNames(oldPrayerNames, newPrayerNames)

                settingsValue?.language = Locale.getDefault().language
                settingsValue?.categories?.removeAll(oldCategoryNames.toSet())
                settingsValue?.categories?.addAll(newCategoryNames)
                settingsValue?.let {
                    updateSettings(it)
                }

                withContext(Dispatchers.Main) {
                    startService()
                }
            }
        }
    }

    fun browserWithCustomTabs() {
        if (browserPackageNameToUse == null)
            browserPackageNameToUse = getPackageNameToUse(getApplication())
    }

    fun getBrowserPackageName() = browserPackageNameToUse

}