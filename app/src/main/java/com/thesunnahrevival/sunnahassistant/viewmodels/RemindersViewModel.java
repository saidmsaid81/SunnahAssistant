package com.thesunnahrevival.sunnahassistant.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.Reminder;
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService;
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager;
import com.thesunnahrevival.sunnahassistant.views.MainActivity;
import com.thesunnahrevival.sunnahassistant.views.ReminderDetailsFragment;
import com.thesunnahrevival.sunnahassistant.views.interfaces.ReminderItemInteractionListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;

public class RemindersViewModel extends SunnahAssistantViewModel implements ReminderItemInteractionListener {

    private ReminderManager mReminderManager = ReminderManager.getInstance();
    private boolean isRescheduleAtLaunch;

    public RemindersViewModel(@NonNull Application application) {
        super(application);
        mReminderManager.createNotificationChannel(application);
        isRescheduleAtLaunch = true;
    }

    public void fetchAllAladhanData() {
        if (mSettings.getValue() != null && isLoadFreshData()) {
            if (mSettings.getValue().isAutomatic()) {
                mRepository.deletePrayerTimesData();
                mRepository.fetchPrayerTimes(
                        mSettings.getValue().getLatitude(), mSettings.getValue().getLongitude(),
                        String.valueOf(month), mYear, mSettings.getValue().getMethod(),
                        mSettings.getValue().getAsrCalculationMethod());
            }
            mRepository.deleteHijriDate();
            mRepository.fetchHijriData(String.valueOf(month), mYear, mSettings.getValue().getHijriOffSet());
            //Save the Month in User Settings to prevent re-fetching the data the current month
            mSettings.getValue().setMonth(month);
            updateSettings(mSettings.getValue());

        }
    }

    public void delete(Reminder reminder) {
        mRepository.deleteReminder(reminder);
    }

    public void insert(Reminder reminder) {
        mRepository.addReminder(reminder);
    }

    public LiveData<List<Reminder>> getReminders(int filter) {
        return mRepository.getAllReminders(filter, mNameOfTheDay);
    }

    public LiveData<String> getErrorMessages() {
        return mRepository.getErrorMessages();
    }

    private boolean isLoadFreshData() {
        if (mSettings.getValue() != null)
            return mSettings.getValue().getMonth() != month && !mSettings.getValue().isFirstLaunch();
        return true;
    }

    public void updateSettings(AppSettings settings) {
        mRepository.updateAppSettings(settings);
    }

    public void setIsLightMode(boolean isLightMode) {
        mRepository.setIsLightMode(isLightMode);
    }

    public void updatePrayerTimeDetails(Reminder oldPrayerDetails, Reminder newPrayerDetails) {
        mRepository.updatePrayerDetails(oldPrayerDetails, newPrayerDetails);
    }

    @Override
    public void onToggleButtonClick(CompoundButton buttonView, boolean isChecked, Reminder reminder) {
        if (buttonView.isPressed() || isRescheduleAtLaunch) {
            if (isChecked)
                scheduleReminder(reminder);
            else
                cancelScheduledReminder(reminder);
        }
        isRescheduleAtLaunch = false;
    }

    @Override
    public void openBottomSheet(View v, Reminder reminder, boolean isNextReminder) {
        if (isNextReminder) {
            Toast.makeText(getApplication(), "Disable the Reminder First before editing...", Toast.LENGTH_LONG).show();
            return;
        }
        ReminderDetailsFragment bottomSheetFragment = new ReminderDetailsFragment();
        if (v.getContext() instanceof MainActivity) {
            FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
            bottomSheetFragment.show(fm, "bottomSheetFragment");
            Bundle bundle = new Bundle();
            bundle.putBoolean("isNew", false);
            if (reminder == null) {
                reminder = new Reminder(
                        "", "", "Not Set", "Sunnah", "Daily", 0, false, new ArrayList<>()
                );
                bundle.putBoolean("isNew", true);
            }
            bundle.putParcelable("Reminder", reminder);
            bottomSheetFragment.setArguments(bundle);
        }

    }

    private void scheduleReminder(Reminder reminder) {
        reminder.setEnabled(true);
        mRepository.setReminderIsEnabled(reminder);
        getApplication().startService(new Intent(getApplication(), NextReminderService.class));
        if (!isRescheduleAtLaunch)
            Toast.makeText(getApplication(), R.string.reminder_successfully_enabled, Toast.LENGTH_SHORT).show();
    }

    public void cancelScheduledReminder(Reminder reminder) {
        reminder.setEnabled(false);
        getApplication().startService(new Intent(getApplication(), NextReminderService.class));
        mReminderManager.cancelScheduledReminder(
                getApplication(), reminder.getId(), reminder.getCategory() + " Reminder",
                reminder.getReminderName(), reminder.getCategory()
        );
        mRepository.setReminderIsEnabled(reminder);
        Toast.makeText(getApplication(), R.string.reminder_successfully_disabled, Toast.LENGTH_SHORT).show();
    }

}


