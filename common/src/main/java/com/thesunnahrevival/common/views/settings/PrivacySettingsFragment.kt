package com.thesunnahrevival.common.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.databinding.FragmentPrivacySettingsBinding
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SunnahAssistantFragment

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

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}