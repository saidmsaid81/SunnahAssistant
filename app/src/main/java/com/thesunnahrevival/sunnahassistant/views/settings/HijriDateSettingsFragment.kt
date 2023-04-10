package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentHijriDateSettingsBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import java.net.MalformedURLException

class HijriDateSettingsFragment : SunnahAssistantFragment() {

    private var inAppBrowser: InAppBrowser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentHijriDateSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_hijri_date_settings, container, false
        )


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

        binding.includeHijriDateInCalendar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                mViewModel.settingsValue?.includeHijriDateInCalendar = isChecked
                mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
            }
        }

        inAppBrowser = InAppBrowser(requireContext(), lifecycleScope)
        binding.hijriInfo.setOnClickListener {
            try {
                inAppBrowser?.launchInAppBrowser(
                    "https://en.wikipedia.org/wiki/Islamic_calendar#Saudi_Arabia's_Umm_al-Qura_calendar",
                    findNavController(),
                    false
                )
            } catch (exception: MalformedURLException) {
                Log.e("MalformedURLException", exception.message.toString())
                Toast.makeText(
                    requireContext(),
                    getString(R.string.something_wrong),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        return binding.root
    }

}