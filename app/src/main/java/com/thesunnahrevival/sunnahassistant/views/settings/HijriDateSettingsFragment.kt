package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.HijriDateSettingsBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

class HijriDateSettingsFragment : SunnahAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: HijriDateSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.hijri_date_settings, container, false
        )
        val myActivity = activity
        if (myActivity != null) {

            mViewModel.getSettings().observe(viewLifecycleOwner) {
                mViewModel.settingsValue = it
                binding.settings = it
            }

            binding.displayHijriDateSwitch.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (buttonView.isPressed) {
                    mViewModel.settingsValue?.isDisplayHijriDate = isChecked
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

}