package com.thesunnahrevival.sunnahassistant.views.settings

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.PrayerTimeSettingsBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.dialogs.ConfirmationDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterLocationDialogFragment
import java.lang.Integer.parseInt

class PrayerTimeSettingsFragment: SettingsFragmentWithPopups(), View.OnClickListener {

    private lateinit var mViewModel: SunnahAssistantViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: PrayerTimeSettingsBinding = DataBindingUtil.inflate(
                inflater, R.layout.prayer_time_settings, container, false)
        val myActivity = activity
        if (myActivity != null) {

            mViewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            mViewModel.getSettings().observe(viewLifecycleOwner, Observer {
                if (it != null){
                    mViewModel.settingsValue = it
                    binding.settings = it
                    binding.setCalculationMethod(resources.getStringArray(R.array.calculation_methods)[it.method])
                    binding.setAsrCalculationMethod(
                            resources.getStringArray(R.array.asr_juristic_method)[it.asrCalculationMethod]
                    )
                    binding.latitudeAdjustmentMethod =
                            resources.getStringArray(R.array.latitude_options)[it.latitudeAdjustmentMethod]
                }
            })

            binding.activatePrayerTimeAlerts.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    mViewModel.settingsValue?.isAutomatic = isChecked
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                }
            }

            binding.locationDetails.setOnClickListener(this)
            binding.calculationDetails.setOnClickListener(this)
            binding.asrCalculationDetails.setOnClickListener(this)
            binding.higherLatitudeDetails.setOnClickListener(this)
            binding.doNotDisturb.setOnClickListener(this)
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.location_details -> {
                val dialogFragment = EnterLocationDialogFragment()
                fragmentManager?.let { dialogFragment.show(it, "dialog") }
            }
            R.id.calculation_details ->
                showPopup(resources.getStringArray(R.array.calculation_methods), R.id.calculation_method, R.id.calculation_details)
            R.id.asr_calculation_details ->
                showPopup(resources.getStringArray(R.array.asr_juristic_method),
                        R.id.asr_calculation_method, R.id.asr_calculation_details)
            R.id.higher_latitude_details ->
                showPopup(resources.getStringArray(R.array.latitude_options),
                        R.id.higher_latitude_method, R.id.higher_latitude_details)
            R.id.do_not_disturb -> {
                if (isNotificationPolicyGranted())
                    showPopup(resources.getStringArray(R.array.do_not_disturb_minutes),
                            R.id.do_not_disturb_minutes, R.id.do_not_disturb)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val calculationMethods = resources.getStringArray(R.array.calculation_methods)
        val asrMethods = resources.getStringArray(R.array.asr_juristic_method)
        val latitudeMethods = resources.getStringArray(R.array.latitude_options)

        when (item?.groupId) {
            R.id.calculation_details -> {
                mViewModel.settingsValue?.method = calculationMethods.indexOf(item.title.toString())
            }
            R.id.asr_calculation_details -> {
                mViewModel.settingsValue?.asrCalculationMethod = asrMethods.indexOf(item.title.toString())
            }
            R.id.higher_latitude_details -> {
                mViewModel.settingsValue?.asrCalculationMethod = latitudeMethods.indexOf(item.title.toString())
            }
            R.id.do_not_disturb -> {
                mViewModel.settingsValue?.doNotDisturbMinutes = parseInt(item.title.toString())
            }
        }
        mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
        return true
    }

    private fun isNotificationPolicyGranted(): Boolean {
        val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Check if the notification policy access has been granted for the app.
            return if (!notificationManager.isNotificationPolicyAccessGranted) {
                showConfirmationDialog()
                false
            } else
                true
        }

        return false
    }

    private fun showConfirmationDialog() {

        ConfirmationDialogFragment.title = getString(R.string.dnd_dialog_title)
        ConfirmationDialogFragment.text = getString(R.string.dnd_message)
        ConfirmationDialogFragment.positiveLabel = getString(R.string.go_to_settings)

        val confirmationDialogFragment = ConfirmationDialogFragment()
        confirmationDialogFragment.mListener = DialogInterface.OnClickListener { dialog, _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent)
                dialog.dismiss()
            }
        }

        fragmentManager?.let { confirmationDialogFragment.show(it, "dialog") }
    }

    override fun onPause() {
        super.onPause()
        mViewModel.updatePrayerTimesData()
    }
}