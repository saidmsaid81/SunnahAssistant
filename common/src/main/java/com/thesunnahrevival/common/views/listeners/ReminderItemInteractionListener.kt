package com.thesunnahrevival.common.views.listeners

import android.view.View
import android.widget.CompoundButton
import com.thesunnahrevival.common.data.model.Reminder

interface ReminderItemInteractionListener {
    fun onMarkAsComplete(buttonView: CompoundButton, isChecked: Boolean, reminder: Reminder?)
    fun launchReminderDetailsFragment(v: View, reminder: Reminder?)
    fun onSwipeToDelete(position: Int)
}