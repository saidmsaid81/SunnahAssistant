package com.thesunnahrevival.sunnahassistant.views.listeners

import android.view.View
import com.thesunnahrevival.sunnahassistant.data.model.ToDo

interface ToDoItemInteractionListener {
    fun onMarkAsComplete(userInitiated: Boolean, isChecked: Boolean?, toDo: ToDo)
    fun launchToDoDetailsFragment(v: View, toDo: ToDo?)
    fun onSwipeToDelete(position: Int, toDo: ToDo)
    fun onSwipeToMarkAsComplete(toDo: ToDo) {
        onMarkAsComplete(true, null, toDo)
    }

    fun showUndoDeleteSnackbar(toDo: ToDo)
    fun showPrayerTimeDeletionError()
}