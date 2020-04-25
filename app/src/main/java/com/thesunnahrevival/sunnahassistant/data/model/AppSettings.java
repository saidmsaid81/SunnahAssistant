package com.thesunnahrevival.sunnahassistant.data.model;

import android.net.Uri;

import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;

import java.util.Arrays;
import java.util.HashSet;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettings {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int method;
    private int asrCalculationMethod;
    private int month = 0;
    private int hijriOffSet = 0;
    private float latitude;
    private float longitude;
    private String formattedAddress;
    private boolean isLightMode = true;
    private boolean isFirstLaunch = true;
    private boolean showNextReminderNotification = true;
    private boolean showOnBoardingTutorial = true;
    private boolean isAutomatic;
    private boolean isDisplayHijriDate = true;
    private int savedSpinnerPosition = 0;
    private boolean isExpandedLayout = true;
    private Uri notificationToneUri;
    private boolean isVibrate = false;
    private int priority = 3;
    private int latitudeAdjustmentMethod = 3;
    private HashSet<String> categories = new HashSet<>( Arrays.asList(SunnahAssistantUtil.UNCATEGORIZED, SunnahAssistantUtil.SUNNAH, SunnahAssistantUtil.PRAYER, SunnahAssistantUtil.OTHER));

    public AppSettings(String formattedAddress, float latitude, float longitude, int method, int asrCalculationMethod, boolean isAutomatic) {
        this.formattedAddress = formattedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.method = method;
        this.asrCalculationMethod = asrCalculationMethod;
        this.isAutomatic = isAutomatic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getAsrCalculationMethod() {
        return asrCalculationMethod;
    }

    public void setAsrCalculationMethod(int asrCalculationMethod) {
        this.asrCalculationMethod = asrCalculationMethod;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getHijriOffSet() {
        return hijriOffSet;
    }

    public void setHijriOffSet(int hijriOffSet) {
        this.hijriOffSet = hijriOffSet;
    }

    public boolean isLightMode() {
        return isLightMode;
    }

    public void setLightMode(boolean lightMode) {
        isLightMode = lightMode;
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public void setFirstLaunch(boolean firstLaunch) {
        isFirstLaunch = firstLaunch;
    }

    public boolean isShowNextReminderNotification() {
        return showNextReminderNotification;
    }

    public void setShowNextReminderNotification(boolean showNextReminderNotification) {
        this.showNextReminderNotification = showNextReminderNotification;
    }

    public boolean isShowOnBoardingTutorial() {
        return showOnBoardingTutorial;
    }

    public void setShowOnBoardingTutorial(boolean showOnBoardingTutorial) {
        this.showOnBoardingTutorial = showOnBoardingTutorial;
    }

    public boolean isDisplayHijriDate() {
        return isDisplayHijriDate;
    }

    public void setDisplayHijriDate(boolean displayHijriDate) {
        isDisplayHijriDate = displayHijriDate;
    }

    public int getSavedSpinnerPosition() {
        return savedSpinnerPosition;
    }

    public void setSavedSpinnerPosition(int savedSpinnerPosition) {
        this.savedSpinnerPosition = savedSpinnerPosition;
    }

    public HashSet<String> getCategories() {
        return categories;
    }

    public void setCategories(HashSet<String> categories) {
        this.categories = categories;
    }

    public boolean isExpandedLayout() {
        return isExpandedLayout;
    }

    public void setExpandedLayout(boolean expandedLayout) {
        isExpandedLayout = expandedLayout;
    }

    public Uri getNotificationToneUri() {
        return notificationToneUri;
    }

    public void setNotificationToneUri(Uri notificationToneUri) {
        this.notificationToneUri = notificationToneUri;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public void setVibrate(boolean vibrate) {
        isVibrate = vibrate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLatitudeAdjustmentMethod() {
        return latitudeAdjustmentMethod;
    }

    public void setLatitudeAdjustmentMethod(int latitudeAdjustmentMethod) {
        this.latitudeAdjustmentMethod = latitudeAdjustmentMethod;
    }
}
