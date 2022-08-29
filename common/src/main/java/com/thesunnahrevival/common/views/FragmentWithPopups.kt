package com.thesunnahrevival.common.views

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import com.google.firebase.analytics.FirebaseAnalytics

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

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundle
        )
    }
}