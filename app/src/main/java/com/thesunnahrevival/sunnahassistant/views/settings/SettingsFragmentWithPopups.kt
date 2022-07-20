package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

abstract class SettingsFragmentWithPopups : SunnahAssistantFragment(),
    PopupMenu.OnMenuItemClickListener {

    fun showPopup(strings: Array<String>, viewId: Int, id: Int) {
        val popupMenu = PopupMenu(context, activity?.findViewById(viewId))
        for ((index, method) in strings.withIndex()) {
            popupMenu.menu.add(id, Menu.NONE, index, method)
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}