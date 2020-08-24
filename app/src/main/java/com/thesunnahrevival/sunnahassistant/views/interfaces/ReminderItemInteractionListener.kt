package com.thesunnahrevival.sunnahassistant.views.interfaces

import android.view.View
import android.widget.CompoundButton
import com.thesunnahrevival.sunnahassistant.data.model.Reminder

interface ReminderItemInteractionListener {
    fun onToggleButtonClick(buttonView: CompoundButton, isChecked: Boolean, reminder: Reminder?)
    fun openBottomSheet(v: View, reminder: Reminder?)
}