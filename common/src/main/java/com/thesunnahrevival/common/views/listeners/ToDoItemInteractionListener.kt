package com.thesunnahrevival.common.views.listeners

import android.view.View
import com.thesunnahrevival.common.data.model.ToDo

interface ToDoItemInteractionListener {
    fun onMarkAsComplete(isPressed: Boolean, isChecked: Boolean?, toDo: ToDo)
    fun launchToDoDetailsFragment(v: View, toDo: ToDo?)
    fun onSwipeToDelete(position: Int, toDo: ToDo)
    fun onSwipeToMarkAsComplete(toDo: ToDo) {
        onMarkAsComplete(true, null, toDo)
    }
}