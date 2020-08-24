package com.thesunnahrevival.sunnahassistant.views.settings

import android.view.Menu
import android.widget.PopupMenu
import androidx.fragment.app.Fragment

abstract class SettingsFragmentWithPopups: Fragment(), PopupMenu.OnMenuItemClickListener {

    fun showPopup(strings: Array<String>, viewId: Int, id: Int ) {
        val popupMenu = PopupMenu(context, activity?.findViewById(viewId))
        for ((index, method) in strings.withIndex()) {
            popupMenu.menu.add(id, Menu.NONE, index, method)
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }
}