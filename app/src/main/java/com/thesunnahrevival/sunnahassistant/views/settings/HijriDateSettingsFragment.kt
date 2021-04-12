package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.HijriDateSettingsBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class HijriDateSettingsFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: HijriDateSettingsBinding = DataBindingUtil.inflate(
                inflater, R.layout.hijri_date_settings, container, false)
        val myActivity = activity
        if (myActivity != null){

            val viewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.getSettings().observe(viewLifecycleOwner, Observer {
                viewModel.settingsValue = it
                binding.settings = it
            })

            binding.displayHijriDateSwitch.setOnCheckedChangeListener{ buttonView: CompoundButton, isChecked: Boolean ->
                if (buttonView.isPressed) {
                    viewModel.settingsValue?.isDisplayHijriDate = isChecked
                    viewModel.settingsValue?.let { viewModel.updateSettings(it) }
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