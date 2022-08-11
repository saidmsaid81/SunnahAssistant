package com.thesunnahrevival.common.views.others

import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SunnahAssistantFragment
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : SunnahAssistantFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val myActivity = activity

        if (myActivity != null) {
            mViewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
                if (settings?.isFirstLaunch == false) {
                    findNavController().navigate(R.id.todayFragment)
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
                        mViewModel.updateSettings(settings)
                    }
                }
            }

            read_privacy_policy.setOnClickListener {
                findNavController().navigate(R.id.privacyPolicyFragment)
            }
        }
    }
}