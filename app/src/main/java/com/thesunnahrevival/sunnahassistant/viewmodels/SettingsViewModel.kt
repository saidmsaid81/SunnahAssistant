package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = SunnahAssistantRepository.getInstance(application)
    val settings: LiveData<AppSettings>
    var isSettingsUpdated: Boolean private set
    var month = TimeDateUtil.getMonthNumber(System.currentTimeMillis())

    private val settingsValue: AppSettings?
        get() = settings.value

    init {
        settings = mRepository.appSettings
        isSettingsUpdated = false
    }

    fun updateDisplayHijriDateSettings(isDisplay: Boolean) {
        settingsValue?.let {
            it.isDisplayHijriDate = isDisplay
            updateSettings()
        }

    }


    fun updateAutomaticPrayerTimeSettings(isEnabled: Boolean) {
        settingsValue?.let {
            if (it.isAutomatic != isEnabled) {
                it.isAutomatic = isEnabled
                updateSettings()
            }
        }

    }

    fun fetchGeocodingData(address: String?) {
        mRepository.fetchGeocodingData(address)
    }

    fun updateGeneratedPrayerTimes() {
        settingsValue?.let {
            if (isLoadFreshData) {
                if (it.isAutomatic) {
                    mRepository.updateGeneratedPrayerTimes(
                            it.latitude, it.longitude, it.toString(), TimeDateUtil.getYear(System.currentTimeMillis()),
                            it.method, it.asrCalculationMethod, it.latitudeAdjustmentMethod)
                    //Save the Month in User Settings to prevent re-fetching the data the current month
                    updateSavedMonth()
                    Toast.makeText(getApplication(), "Refreshing", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateSavedMonth() {
        settingsValue?.let{
            if (it.month != month) {
                it.month = month
                updateSettings()
            }
        }

    }

    fun updateSavedSpinnerPosition(position: Int) {
        settingsValue?.let {
            if (it.savedSpinnerPosition != position) {
                it.savedSpinnerPosition = position
                updateSettings()
            }
        }
    }

    fun updateIsShowOnBoardingTutorial(isShowOnBoardingTutorial: Boolean) {
        settingsValue?.let {
            if (it.isShowOnBoardingTutorial != isShowOnBoardingTutorial) {
                it.isShowOnBoardingTutorial = isShowOnBoardingTutorial
                updateSettings()
            }
        }
    }

    fun observeGeocodingApiData(): LiveData<GeocodingData> {
        return mRepository.geocodingData
    }

    fun updateLocationDetails(data: GeocodingData) {
        settingsValue?.let {
            val result = data.results[0]
            it.formattedAddress = result.formattedAddress
            it.latitude = result.geometry.location.lat
            it.longitude = result.geometry.location.lng
            updateSettings()

        }
    }

    fun updateCalculationMethod(calculationMethod: Int) {
        settingsValue?.let {
            if (it.method != calculationMethod) {
                it.method = calculationMethod
                updateSettings()
            }
        }

    }

    fun updateAsrCalculationMethod(calculationMethod: Int) {
        settingsValue?.let {
            if (it.asrCalculationMethod != calculationMethod) {
                it.asrCalculationMethod = calculationMethod
                updateSettings()
            }
        }
    }

    fun updateHigherLatitudeMethod(method: Int) {
        settingsValue?.let {
            if (it.latitudeAdjustmentMethod != method) {
                it.latitudeAdjustmentMethod = method
                updateSettings()
            }
        }
    }

    fun updatePrayerTimesData() {
        val settings = settingsValue
        if (isSettingsUpdated && settings != null && !settings.formattedAddress.matches("Location cannot be empty".toRegex())) {
            mRepository.deletePrayerTimesData()
            if (settings.isAutomatic) {
                settings.categories.remove(SunnahAssistantUtil.PRAYER)
                mRepository.generatePrayerTimes(
                        settings.latitude, settings.longitude, settings.month.toString(),
                        TimeDateUtil.getYear(System.currentTimeMillis()), settings.method, settings.asrCalculationMethod,
                        settings.latitudeAdjustmentMethod
                )
            }
            else
                settings.categories.add(SunnahAssistantUtil.PRAYER)
            updateCategories(settings.categories)
        }
    }

    fun updateLayout(isExpandedLayout: Boolean) {
        settingsValue?.let {
            if (it.isExpandedLayout != isExpandedLayout) {
                it.isExpandedLayout = isExpandedLayout
                updateSettings()
            }
        }
    }

    fun updateTheme(isLightMode: Boolean) {
        settingsValue?.let {
            if (it.isLightMode != isLightMode) {
                it.isLightMode = isLightMode
                updateSettings()
            }
        }
    }

    fun updateCategories(categories: HashSet<String>) {
        settingsValue?.let {
            it.categories = categories
            updateSettings()
        }
    }

    fun updatedDeletedCategories(deletedCategories: ArrayList<String>) {
        if (deletedCategories.isNotEmpty()) {
            mRepository.updateDeletedCategories(deletedCategories)
        }
    }

    fun updateNotificationSettings(notificationUri: Uri?, isVibrate: Boolean, priority: Int) {
        mRepository.updateNotificationSettings(notificationUri, isVibrate, priority)
    }

    fun updateRingtone(uri: Uri?) {
        settingsValue?.let {
           it.notificationToneUri = uri
            updateSettings()
        }
    }

    fun updateVibrationSettings(isVibrate: Boolean) {
        settingsValue?.let {
            it.isVibrate = isVibrate
            updateSettings()
        }
    }

    fun updateNotificationPriority(priority: Int) {
       settingsValue?.let {
           it.priority = priority
           updateSettings()
       }


    }

    fun showStickyNotification(isShow: Boolean) {
        settingsValue?.let {
            if (it.isShowNextReminderNotification != isShow) {
                it.isShowNextReminderNotification = isShow
                updateSettings()
            }
        }
    }

    fun updateIsFirstLaunch(isFirstLaunch :Boolean){
        settingsValue?.let {
            it.isFirstLaunch = isFirstLaunch
            updateSettings()
        }
    }

    fun updateIsAfterUpdate(isAfterUpdate :Boolean){
        settingsValue?.let {
            it.isAfterUpdate = isAfterUpdate
            updateSettings()
        }
    }

    private fun updateSettings() {
        isSettingsUpdated = true
        settingsValue?.let { mRepository.updateAppSettings(it) }
    }

    val isDeviceOffline: Boolean
        get() {
            val connectivityManager = getApplication<Application>().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo == null ||
                    !connectivityManager.activeNetworkInfo.isConnected
        }

    private val isLoadFreshData: Boolean
        get() = if (settingsValue != null)
            settingsValue?.month != month
        else true


}