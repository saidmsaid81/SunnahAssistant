package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

open class SunnahAssistantFragment : Fragment() {
    protected lateinit var mainActivityViewModel: SunnahAssistantViewModel

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivityViewModel = ViewModelProvider(requireActivity())[SunnahAssistantViewModel::class.java]
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun getActionBarSize(): Int {
        val typedValue = TypedValue()
        return if (requireActivity().theme.resolveAttribute(
                android.R.attr.actionBarSize,
                typedValue,
                true
            )
        ) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            resources.getDimensionPixelSize(R.dimen.action_bar_size)
        }
    }
    override fun onResume() {
        super.onResume()
        handleEdgeToEdge()
        try {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
            (requireActivity() as MainActivity).firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundle
            )
        } catch (exception: IllegalArgumentException) {
            exception.printStackTrace()
        }

    }

    protected fun handleEdgeToEdge() {
        val actionBarHeight = getActionBarSize()
        val bottomNavigationView =
            (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val bottomNavHeight =
            if (bottomNavigationView?.visibility == View.VISIBLE) actionBarHeight else 0
        view?.setPadding(
            view?.paddingLeft ?: 0,
            actionBarHeight,
            view?.paddingRight ?: 0,
            bottomNavHeight
        )
    }
}