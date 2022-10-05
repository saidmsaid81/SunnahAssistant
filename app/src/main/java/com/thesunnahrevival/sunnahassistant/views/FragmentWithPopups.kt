package com.thesunnahrevival.sunnahassistant.views

import android.view.Menu
import android.widget.PopupMenu

abstract class FragmentWithPopups : SunnahAssistantFragment(),
    PopupMenu.OnMenuItemClickListener {

    fun showPopup(
        strings: Array<String>,
        viewId: Int,
        groupId: Int
    ) {
        val popupMenu = PopupMenu(context, requireActivity().findViewById(viewId))
        for ((index, title) in strings.withIndex()) {
            val menuItem = popupMenu.menu.add(groupId, Menu.NONE, index, title)
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }
}