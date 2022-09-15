package com.thesunnahrevival.common.views.listeners

import android.view.View
import com.thesunnahrevival.common.data.model.Reminder

interface ReminderItemInteractionListener {
    fun onMarkAsComplete(isPressed: Boolean, isChecked: Boolean?, reminder: Reminder)
    fun launchReminderDetailsFragment(v: View, reminder: Reminder?)
    fun onSwipeToDelete(position: Int, reminder: Reminder)
    fun onSwipeToMarkAsComplete(reminder: Reminder) {
        onMarkAsComplete(true, null, reminder)
    }
}