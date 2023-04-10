package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentPrivacySettingsBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

class PrivacySettingsFragment : SunnahAssistantFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentPrivacySettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_privacy_settings, container, false
        )
        binding.collectDataSwitch.text = HtmlCompat.fromHtml(
            getString(R.string.share_anonymous_usage_data),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        val myActivity = activity
        if (myActivity != null) {

            mViewModel.getSettings().observe(viewLifecycleOwner) {
                mViewModel.settingsValue = it
                binding.settings = it
            }

            binding.collectDataSwitch.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (buttonView.isPressed) {
                    (myActivity as MainActivity).firebaseAnalytics.setAnalyticsCollectionEnabled(
                        isChecked
                    )
                    mViewModel.settingsValue?.shareAnonymousUsageData = isChecked
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                }
            }

            binding.readPrivacyPolicy.setOnClickListener {
                findNavController().navigate(R.id.privacyPolicyFragment)
            }
        }

        return binding.root
    }
}