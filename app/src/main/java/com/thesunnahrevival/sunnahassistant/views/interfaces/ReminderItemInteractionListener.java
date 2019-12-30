package com.thesunnahrevival.sunnahassistant.views.interfaces;

import android.view.View;
import android.widget.CompoundButton;

import com.thesunnahrevival.sunnahassistant.data.Reminder;

public interface ReminderItemInteractionListener {
    void onToggleButtonClick(CompoundButton buttonView, boolean isChecked, Reminder reminder);

    void openBottomSheet(View v, Reminder reminder, boolean isNextReminder);
}
