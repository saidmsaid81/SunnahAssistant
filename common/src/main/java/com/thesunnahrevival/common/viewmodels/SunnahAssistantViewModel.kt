package com.thesunnahrevival.common.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.lifecycle.*
import androidx.paging.*
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.data.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.GeocodingData
import com.thesunnahrevival.common.data.model.ToDo
import com.thesunnahrevival.common.services.NextToDoService
import com.thesunnahrevival.common.utilities.generateLocalDatefromDate
import com.thesunnahrevival.common.utilities.sunnahReminders
import com.thesunnahrevival.common.utilities.supportedLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*


class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)

    var selectedToDo =
        ToDo(
            name = "", frequency = Frequency.OneTime,
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

    val selectedToDoDate: LocalDate
        get() {
            return generateLocalDatefromDate(
                Date(
                    mutableReminderParameters.value?.first ?: System.currentTimeMillis()
                )
            )
        }
    val categoryToDisplay: String
        get() {
            return mutableReminderParameters.value?.second ?: ""
        }

    val triggerCalendarUpdate = MutableLiveData<Boolean>()

    fun setToDoParameters(date: Long? = null, category: String? = null) {
        val currentDateParameter =
            mutableReminderParameters.value?.first ?: System.currentTimeMillis()
        val currentCategoryParameter = mutableReminderParameters.value?.second ?: ""
        mutableReminderParameters.value =
            Pair(date ?: currentDateParameter, category ?: currentCategoryParameter)
    }

    fun insertToDo(toDo: ToDo, updateCalendar: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.insertToDo(toDo)
            withContext(Dispatchers.Main) {
                startService()
                if (updateCalendar)
                    triggerCalendarUpdate.value = true
            }
        }
    }

    fun deleteToDo(toDo: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteToDO(toDo)
            withContext(Dispatchers.Main) {
                startService()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun thereToDosOnDay(dayOfWeek: String, dayOfMonth: Int, month: Int, year: Int): Boolean {
        val excludePrayer = getContext().getString(R.string.prayer)
        return mRepository.thereToDosOnDay(excludePrayer, dayOfWeek, dayOfMonth, month, year)
    }

    fun getToDo(id: Int) = mRepository.getToDo(id)

    fun getIncompleteToDos(): LiveData<PagingData<ToDo>> {
        return Transformations.switchMap(mutableReminderParameters) { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getIncompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun getCompleteToDos(): LiveData<PagingData<ToDo>> {
        return Transformations.switchMap(mutableReminderParameters) { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getCompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun updatePrayerTimeDetails(oldPrayerDetails: ToDo, newPrayerDetails: ToDo) {
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
                NextToDoService::class.java
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
                    mRepository.updateToDo(
                        reminder.id,
                        reminder.name,
                        reminder.additionalInfo,
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
}