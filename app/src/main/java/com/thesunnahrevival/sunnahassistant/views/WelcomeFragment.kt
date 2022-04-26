package com.thesunnahrevival.sunnahassistant.views

import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val myActivity = activity

        if (myActivity != null) {
            val viewModel =
                ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
                if (settings?.isFirstLaunch == false) {
                    findNavController().navigate(R.id.mainFragment)
                }

                quick_setup_button.setOnClickListener {
                    if (settings != null) {
                        checkbox.visibility = View.INVISIBLE
                        privacy_policy.visibility = View.INVISIBLE
                        read_privacy_policy.visibility = View.INVISIBLE
                        progress_bar.visibility = View.VISIBLE
                        try {
                            settings.notificationToneUri =
                                RingtoneManager.getActualDefaultRingtoneUri(
                                    context, RingtoneManager.TYPE_NOTIFICATION
                                )
                        } catch (exception: SecurityException) {
                            Log.i("Info", "Notification tone set to default")
                        }

                        settings.isFirstLaunch = false
                        settings.shareAnonymousUsageData = checkbox.isChecked
                        (myActivity as MainActivity).firebaseAnalytics.setAnalyticsCollectionEnabled(
                            checkbox.isChecked
                        )
                        viewModel.updateSettings(settings)
                    }
                }
            }

            read_privacy_policy.setOnClickListener {
                findNavController().navigate(R.id.privacyPolicyFragment)
            }
        }
    }
}