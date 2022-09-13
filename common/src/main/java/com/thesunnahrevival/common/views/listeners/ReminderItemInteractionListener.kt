package com.thesunnahrevival.common.views.listeners

import android.view.View
import com.thesunnahrevival.common.data.model.Reminder

interface ReminderItemInteractionListener {
    fun onMarkAsComplete(isPressed: Boolean, isChecked: Boolean?, position: Int)
    fun launchReminderDetailsFragment(v: View, reminder: Reminder?)
    fun onSwipeToDelete(position: Int)
    fun onSwipeToMarkAsComplete(position: Int) {
        onMarkAsComplete(true, null, position)
    }
}