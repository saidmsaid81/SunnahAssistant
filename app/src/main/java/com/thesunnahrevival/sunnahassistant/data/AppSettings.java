package com.thesunnahrevival.sunnahassistant.data;

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
}
