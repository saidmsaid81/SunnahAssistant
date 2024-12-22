package com.thesunnahrevival.sunnahassistant.views.others

import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.databinding.FragmentWelcomeBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
class WelcomeFragment : SunnahAssistantFragment() {

    private var _welcomeFragmentBinding: FragmentWelcomeBinding? = null
    private val welcomeFragmentBinding get() = _welcomeFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _welcomeFragmentBinding = FragmentWelcomeBinding.inflate(inflater)
        return welcomeFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
            if (settings?.isFirstLaunch == false) {
                findNavController().navigate(R.id.todayFragment)
            }

            welcomeFragmentBinding.quickSetupButton.setOnClickListener {
                if (settings != null) {
                    welcomeFragmentBinding.checkbox.visibility = View.INVISIBLE
                    welcomeFragmentBinding.privacyPolicy.visibility = View.INVISIBLE
                    welcomeFragmentBinding.readPrivacyPolicy.visibility = View.INVISIBLE
                    welcomeFragmentBinding.progressBar.visibility = View.VISIBLE
                    try {
                        settings.notificationToneUri =
                            RingtoneManager.getActualDefaultRingtoneUri(
                                context, RingtoneManager.TYPE_NOTIFICATION
                            )
                    } catch (exception: SecurityException) {
                        Log.i("Info", "Notification tone set to default")
                    }

                    settings.isFirstLaunch = false
                    settings.shareAnonymousUsageData = welcomeFragmentBinding.checkbox.isChecked
                    (requireActivity() as MainActivity).firebaseAnalytics.setAnalyticsCollectionEnabled(
                        welcomeFragmentBinding.checkbox.isChecked
                    )
                    mViewModel.updateSettings(settings)
                }
            }
        }

        welcomeFragmentBinding.readPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _welcomeFragmentBinding = null
    }
}