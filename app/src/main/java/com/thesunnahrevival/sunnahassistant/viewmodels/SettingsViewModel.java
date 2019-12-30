package com.thesunnahrevival.sunnahassistant.viewmodels;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.AppSettings;
import com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask;

import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class SettingsViewModel extends SunnahAssistantViewModel {

    public MutableLiveData<Boolean> closeWelcomeScreen = new MutableLiveData<>();
    public int mCounter = 1;
    private MutableLiveData<String> offsetValue = new MutableLiveData<>();
    private MutableLiveData<String> hijriChangeErrorMessage = new MutableLiveData<>();
    private MutableLiveData<String> updatePrayerSettingsMessage = new MutableLiveData<>();
    private int mOffsetSavedValue;


    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    public void setOffsetSavedValue(int value) {
        mOffsetSavedValue = value;
        offsetValue.setValue(String.valueOf(value));
    }

    public LiveData<String> getOffSet() {
        return offsetValue;
    }

    public LiveData<String> getHijriChangeErrorMessage() {
        return hijriChangeErrorMessage;
    }

    public void updateHijriDate(int value) {
        if (value < -30 || value > 30)
            hijriChangeErrorMessage.setValue("Error! Value should be between -30 and 30");

        else if (isDeviceOffline(getApplication()))
            hijriChangeErrorMessage.setValue("Error Updating. Please Check your internet connection.");

        else if (value != mOffsetSavedValue) {
            hijriChangeErrorMessage.setValue("Updating...");
            mRepository.deleteHijriDate();
            if (mSettings.getValue() != null) {
                mSettings.getValue().setHijriOffSet(value);
                updateSettings();
                hijriChangeErrorMessage.setValue(null);
            }
            mOffsetSavedValue = value;
        } else
            hijriChangeErrorMessage.setValue("No changes made");
    }


    public LiveData<String> getCityName() {
        return Transformations.map(mSettings, AppSettings::getFormattedAddress);
    }

    public LiveData<String> getLocationUpdateMessage() {
        return Transformations.map(mRepository.getGeocodingData(), message -> {
            AppSettings settings = mSettings.getValue();
            if (message != null && !message.getResults().isEmpty() && settings != null) {
                float latitude = message.getResults().get(0).getGeometry().getLocation().getLat();
                float longitude = message.getResults().get(0).getGeometry().getLocation().getLng();
                settings.setLatitude(latitude);
                settings.setLongitude(longitude);
                settings.setFormattedAddress(message.getResults().get(0).getFormattedAddress());
                savePrayerTimesSettings();
                return "Successful:" + message.getResults().get(0).getFormattedAddress();
            }
            return null;
        });
    }

    public LiveData<String> getPrayerUpdateMessage() {
        return updatePrayerSettingsMessage;
    }

    public void setPrayerSettingsManually() {
        if (mSettings.getValue() != null) {
            mSettings.getValue().setAutomatic(false);
            mSettings.getValue().setFormattedAddress("");
            boolean shouldCloseWelcomeScreen = false;
            if (mSettings.getValue().isFirstLaunch()) {
                mSettings.getValue().setFirstLaunch(false);
                shouldCloseWelcomeScreen = true;
            }
            mSettings.getValue().setMonth(0);
            updateSettings();
            mRepository.deletePrayerTimesData();
            updatePrayerSettingsMessage.setValue(getApplication().getString(R.string.successfully_updated));
            if (shouldCloseWelcomeScreen)
                closeWelcomeScreen.setValue(true);
        }
    }

    private void savePrayerTimesSettings() {
        if (mCounter == 0 && !isDeviceOffline(getApplication())) {
            mCounter++;
            mRepository.deletePrayerTimesData();
            updatePrayerSettingsMessage.setValue("Updating...");
            Runnable runnable = () -> {
                AppSettings settings = mSettings.getValue();
                if (GeneralSaveDataAsyncTask.prayerDeleteTaskComplete && settings != null) {
                    settings.setAutomatic(true);
                    settings.setMonth(0); //Reset month to allow refreshing data
                    boolean shouldCloseWelcomeScreen = false;
                    if (mSettings.getValue().isFirstLaunch()) {
                        mSettings.getValue().setFirstLaunch(false);
                        shouldCloseWelcomeScreen = true;
                    }
                    updateSettings();
                    updatePrayerSettingsMessage.setValue("Successfully Updated.");
                    if (shouldCloseWelcomeScreen)
                        closeWelcomeScreen.setValue(true);
                }
            };
            new Handler().postDelayed(runnable, 2000);
        } else if (isDeviceOffline(getApplication())) {
            updatePrayerSettingsMessage.setValue(getApplication().getString(R.string.no_internet_connection));
        } else if (mCounter != 0)
            updatePrayerSettingsMessage.setValue(getApplication().getString(R.string.no_changes));


    }

    /**
     * Checks to see if the location input has changed by comparing it with
     * what is stored in settings
     */
    private boolean validateLocationChanged(String address) {
        return mSettings.getValue() != null && !mSettings.getValue().getFormattedAddress().matches(address);
    }

    public void initiateFetchingGeocodingData(String address) {
        if (isDeviceOffline(getApplication()))
            updatePrayerSettingsMessage.setValue(getApplication().getString(R.string.error_updating_location));
        else if (TextUtils.isEmpty(address))
            updatePrayerSettingsMessage.setValue("Location cannot be empty");
        else if (validateLocationChanged(address)) {
            mCounter = 0;
            mRepository.fetchGeocodingData(address);
            updatePrayerSettingsMessage.setValue("Updating...");
        } else
            savePrayerTimesSettings();
    }

    private boolean isDeviceOffline(Context context) {
        ConnectivityManager connectivityManager = (
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));

        return connectivityManager.getActiveNetworkInfo() == null ||
                !connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void updateSettings() {
        mRepository.updateAppSettings(mSettings.getValue());
    }

}
