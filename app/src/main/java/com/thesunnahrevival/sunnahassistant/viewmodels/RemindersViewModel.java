package com.thesunnahrevival.sunnahassistant.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;
import com.thesunnahrevival.sunnahassistant.views.MainActivity;
import com.thesunnahrevival.sunnahassistant.views.ReminderDetailsFragment;
import com.thesunnahrevival.sunnahassistant.views.interfaces.ReminderItemInteractionListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class RemindersViewModel extends AndroidViewModel implements ReminderItemInteractionListener {

    private boolean isRescheduleAtLaunch;
    private final SunnahAssistantRepository mRepository;

    public RemindersViewModel(@NonNull Application application) {
        super(application);
        isRescheduleAtLaunch = true;
        mRepository = SunnahAssistantRepository.getInstance(application);
    }

    public void addInitialReminders(){
        mRepository.addInitialReminders();
    }

    public void delete(Reminder reminder) {
        mRepository.deleteReminder(reminder);
    }

    public void insert(Reminder reminder) {
        mRepository.addReminder(reminder);
    }

    public LiveData<List<Reminder>> getReminders(int frequencyFilter) {
        return mRepository.getAllReminders(frequencyFilter, TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()));
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

        SunnahAssistantUtil.updateHijriDateWidgets(getApplication());
        SunnahAssistantUtil.updateTodayRemindersWidgets(getApplication());
    }

    @Override
    public void openBottomSheet(View v, Reminder reminder) {
        ReminderDetailsFragment bottomSheetFragment = new ReminderDetailsFragment();
        if (v.getContext() instanceof MainActivity) {
            FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
            bottomSheetFragment.show(fm, "bottomSheetFragment");
            Bundle bundle = new Bundle();
            bundle.putBoolean("isNew", false);
            if (reminder == null) {
                reminder = new Reminder(
                        "", "", null, SunnahAssistantUtil.UNCATEGORIZED, SunnahAssistantUtil.ONE_TIME, false, 0, null, null, 0, null
                );
                bundle.putBoolean("isNew", true);
            }
            bundle.putParcelable("Reminder", reminder);
            bottomSheetFragment.setArguments(bundle);
        }

    }

    public void scheduleReminder(Reminder reminder) {
        reminder.setEnabled(true);
        mRepository.setReminderIsEnabled(reminder);
        getApplication().startService(new Intent(getApplication(), NextReminderService.class));
    }

    public void cancelScheduledReminder(Reminder reminder) {
        reminder.setEnabled(false);
        mRepository.setReminderIsEnabled(reminder);
        getApplication().startService(new Intent(getApplication(), NextReminderService.class));
    }

}


