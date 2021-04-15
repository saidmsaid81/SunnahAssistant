package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentPrivacySettingsBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class PrivacySettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPrivacySettingsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_privacy_settings, container, false)
        binding.collectDataSwitch.text = Html.fromHtml(getString(R.string.share_anonymous_usage_data))

        val myActivity = activity
        if (myActivity != null){

            val viewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.getSettings().observe(viewLifecycleOwner, {
                viewModel.settingsValue = it
                binding.settings = it
            })

            binding.collectDataSwitch.setOnCheckedChangeListener{ buttonView: CompoundButton, isChecked: Boolean ->
                if (buttonView.isPressed) {
                    (myActivity as MainActivity).firebaseAnalytics.setAnalyticsCollectionEnabled(isChecked)
                    viewModel.settingsValue?.shareAnonymousUsageData = isChecked
                    viewModel.settingsValue?.let { viewModel.updateSettings(it) }
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