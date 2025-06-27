package com.thesunnahrevival.sunnahassistant.data.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.sunnahassistant.BuildConfig
import java.util.Date
import java.util.TreeSet

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey(autoGenerate = true) var id: Int = 1,
    var formattedAddress: String? = "",
    var latitude: Float = 0F,
    var longitude: Float = 0F,
    @ColumnInfo(name = "method") var calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    var asrCalculationMethod: Madhab = Madhab.SHAFI,
    @ColumnInfo(name = "isAutomatic") var isAutomaticPrayerAlertsEnabled: Boolean = false,
    var enablePrayerTimeAlertsFor: BooleanArray = BooleanArray(5) { true },
    var prayerTimeOffsetsInMinutes: IntArray = IntArray(5) { 0 },
    var month: Int = 12,
    var hijriOffSet: Int = 0,
    var isLightMode: Boolean = true,
    var isFirstLaunch: Boolean = true,
    @ColumnInfo(name = "showNextReminderNotification") var showNextToDoNotification: Boolean = true,
    var showOnBoardingTutorial: Boolean = true,
    var isDisplayHijriDate: Boolean = true,
    var savedSpinnerPosition: Int = 0,
    var isExpandedLayout: Boolean = true,
    var notificationToneUri: Uri? = null,
    var isVibrate: Boolean = false,
    var priority: Int = 3,
    var latitudeAdjustmentMethod: Int = 2,
    var isShowHijriDateWidget: Boolean = true,
    @ColumnInfo(name = "isShowNextReminderWidget") var isShowNextToDoWidget: Boolean = true,
    var isAfterUpdate: Boolean = false,
    var appVersionCode: Int = BuildConfig.VERSION_CODE,
    var appVersion: String = BuildConfig.VERSION_NAME,
    var categories: TreeSet<String>? = null,
    var language: String = "en",
    var doNotDisturbMinutes: Int = 0,
    var useReliableAlarms: Boolean = true,
    var numberOfLaunches: Int = 0,
    var shareAnonymousUsageData: Boolean = true,
    @ColumnInfo(name = "generatePrayerRemindersAfter") var generatePrayerToDosAfter: Date = Date(),
    var includeHijriDateInCalendar: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    var hideDownloadFilePrompt: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppSettings

        if (id != other.id) return false
        if (formattedAddress != other.formattedAddress) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (calculationMethod != other.calculationMethod) return false
        if (asrCalculationMethod != other.asrCalculationMethod) return false
        if (isAutomaticPrayerAlertsEnabled != other.isAutomaticPrayerAlertsEnabled) return false
        if (!enablePrayerTimeAlertsFor.contentEquals(other.enablePrayerTimeAlertsFor)) return false
        if (!prayerTimeOffsetsInMinutes.contentEquals(other.prayerTimeOffsetsInMinutes)) return false
        if (month != other.month) return false
        if (hijriOffSet != other.hijriOffSet) return false
        if (isLightMode != other.isLightMode) return false
        if (isFirstLaunch != other.isFirstLaunch) return false
        if (showNextToDoNotification != other.showNextToDoNotification) return false
        if (showOnBoardingTutorial != other.showOnBoardingTutorial) return false
        if (isDisplayHijriDate != other.isDisplayHijriDate) return false
        if (savedSpinnerPosition != other.savedSpinnerPosition) return false
        if (isExpandedLayout != other.isExpandedLayout) return false
        if (notificationToneUri != other.notificationToneUri) return false
        if (isVibrate != other.isVibrate) return false
        if (priority != other.priority) return false
        if (latitudeAdjustmentMethod != other.latitudeAdjustmentMethod) return false
        if (isShowHijriDateWidget != other.isShowHijriDateWidget) return false
        if (isShowNextToDoWidget != other.isShowNextToDoWidget) return false
        if (isAfterUpdate != other.isAfterUpdate) return false
        if (appVersionCode != other.appVersionCode) return false
        if (appVersion != other.appVersion) return false
        if (categories != other.categories) return false
        if (language != other.language) return false
        if (doNotDisturbMinutes != other.doNotDisturbMinutes) return false
        if (useReliableAlarms != other.useReliableAlarms) return false
        if (numberOfLaunches != other.numberOfLaunches) return false
        if (shareAnonymousUsageData != other.shareAnonymousUsageData) return false
        if (generatePrayerToDosAfter != other.generatePrayerToDosAfter) return false
        if (includeHijriDateInCalendar != other.includeHijriDateInCalendar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (formattedAddress?.hashCode() ?: 0)
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + calculationMethod.hashCode()
        result = 31 * result + asrCalculationMethod.hashCode()
        result = 31 * result + isAutomaticPrayerAlertsEnabled.hashCode()
        result = 31 * result + enablePrayerTimeAlertsFor.contentHashCode()
        result = 31 * result + prayerTimeOffsetsInMinutes.contentHashCode()
        result = 31 * result + month
        result = 31 * result + hijriOffSet
        result = 31 * result + isLightMode.hashCode()
        result = 31 * result + isFirstLaunch.hashCode()
        result = 31 * result + showNextToDoNotification.hashCode()
        result = 31 * result + showOnBoardingTutorial.hashCode()
        result = 31 * result + isDisplayHijriDate.hashCode()
        result = 31 * result + savedSpinnerPosition
        result = 31 * result + isExpandedLayout.hashCode()
        result = 31 * result + (notificationToneUri?.hashCode() ?: 0)
        result = 31 * result + isVibrate.hashCode()
        result = 31 * result + priority
        result = 31 * result + latitudeAdjustmentMethod
        result = 31 * result + isShowHijriDateWidget.hashCode()
        result = 31 * result + isShowNextToDoWidget.hashCode()
        result = 31 * result + isAfterUpdate.hashCode()
        result = 31 * result + appVersionCode
        result = 31 * result + appVersion.hashCode()
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + language.hashCode()
        result = 31 * result + doNotDisturbMinutes
        result = 31 * result + useReliableAlarms.hashCode()
        result = 31 * result + numberOfLaunches
        result = 31 * result + shareAnonymousUsageData.hashCode()
        result = 31 * result + generatePrayerToDosAfter.hashCode()
        return result
    }
}