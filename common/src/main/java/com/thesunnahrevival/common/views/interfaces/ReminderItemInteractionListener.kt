package com.thesunnahrevival.common.views.interfaces

import android.view.View
import android.widget.CompoundButton
import com.thesunnahrevival.common.data.model.Reminder

interface ReminderItemInteractionListener {
    fun onToggleButtonClick(buttonView: CompoundButton, isChecked: Boolean, reminder: Reminder?)
    fun openBottomSheet(v: View, reminder: Reminder?)
}