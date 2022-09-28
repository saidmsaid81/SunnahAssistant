package com.thesunnahrevival.common.views.settings

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
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.databinding.PrayerTimeSettingsBinding
import com.thesunnahrevival.common.views.FragmentWithPopups
import com.thesunnahrevival.common.views.dialogs.ConfirmationDialogFragment
import com.thesunnahrevival.common.views.dialogs.EnterLocationDialogFragment
import com.thesunnahrevival.common.views.dialogs.EnterOffsetFragment
import com.thesunnahrevival.common.views.dialogs.EnterOffsetFragment.Companion.CURRENT_VALUE
import java.lang.Integer.parseInt
import java.util.*

open class PrayerTimeSettingsFragment : FragmentWithPopups(), View.OnClickListener,
    EnterOffsetFragment.EnterOffsetFragmentListener {

    private val yesNoOptions = arrayOf("", "")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: PrayerTimeSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.prayer_time_settings, container, false
        )

        mViewModel.isPrayerSettingsUpdated = false
        binding.prayers = resources.getStringArray(R.array.prayer_names)

        mViewModel.getSettings().observe(viewLifecycleOwner) {
            if (it != null) {
                mViewModel.settingsValue = it
                binding.settings = it
                binding.setCalculationMethod(resources.getStringArray(R.array.calculation_methods)[it.calculationMethod.ordinal])
                binding.setAsrCalculationMethod(
                    resources.getStringArray(R.array.asr_juristic_method)[it.asrCalculationMethod.ordinal]
                )
                binding.latitudeAdjustmentMethod =
                    resources.getStringArray(R.array.latitude_options)[it.latitudeAdjustmentMethod]
            }
        }

        binding.activatePrayerTimeAlerts.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                val settingsValue = mViewModel.settingsValue ?: return@setOnCheckedChangeListener
                binding.activatePrayerTimeAlerts.isChecked =
                    settingsValue.isAutomaticPrayerAlertsEnabled
                settingsValue.isAutomaticPrayerAlertsEnabled = isChecked
                if (isChecked)
                    settingsValue.generatePrayerRemindersAfter =
                        Date(System.currentTimeMillis() - 86400000)
                updateSettings()
            }
        }

        yesNoOptions[0] = getString(R.string.yes)
        yesNoOptions[1] = getString(R.string.no)
        binding.locationDetails.setOnClickListener(this)
        binding.calculationDetails.setOnClickListener(this)
        binding.asrCalculationDetails.setOnClickListener(this)
        binding.higherLatitudeDetails.setOnClickListener(this)
        binding.doNotDisturb.setOnClickListener(this)
        binding.fajrEnableAlerts.setOnClickListener(this)
        binding.dhuhrEnableAlerts.setOnClickListener(this)
        binding.maghribEnableAlerts.setOnClickListener(this)
        binding.asrEnableAlerts.setOnClickListener(this)
        binding.maghribEnableAlerts.setOnClickListener(this)
        binding.ishaEnableAlerts.setOnClickListener(this)
        binding.fajrOffsetSettings.setOnClickListener(this)
        binding.dhuhrOffsetSettings.setOnClickListener(this)
        binding.asrOffsetSettings.setOnClickListener(this)
        binding.maghribOffsetSettings.setOnClickListener(this)
        binding.ishaOffsetSettings.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(v: View?) {
        val prayerOffsetViews = arrayOf(
            R.id.fajr_offset_settings,
            R.id.dhuhr_offset_settings,
            R.id.asr_offset_settings,
            R.id.maghrib_offset_settings,
            R.id.isha_offset_settings
        )
        when (v?.id) {
            R.id.location_details -> {
                val dialogFragment = EnterLocationDialogFragment()
                dialogFragment.show(requireActivity().supportFragmentManager, "dialog")
            }
            R.id.calculation_details ->
                showPopup(
                    resources.getStringArray(R.array.calculation_methods),
                    R.id.calculation_method,
                    R.id.calculation_details
                )
            R.id.asr_calculation_details ->
                showPopup(
                    resources.getStringArray(R.array.asr_juristic_method),
                    R.id.asr_calculation_method, R.id.asr_calculation_details
                )
            R.id.higher_latitude_details ->
                showPopup(
                    resources.getStringArray(R.array.latitude_options),
                    R.id.higher_latitude_method, R.id.higher_latitude_details
                )
            R.id.do_not_disturb -> {
                if (isNotificationPolicyGranted())
                    showPopup(
                        resources.getStringArray(R.array.do_not_disturb_minutes),
                        R.id.do_not_disturb_minutes, R.id.do_not_disturb
                    )
            }
            R.id.fajr_enable_alerts ->
                showPopup(
                    yesNoOptions,
                    R.id.fajr_enable_alerts_value, R.id.fajr_enable_alerts
                )
            R.id.dhuhr_enable_alerts ->
                showPopup(
                    yesNoOptions,
                    R.id.dhuhr_enable_alerts_value, R.id.dhuhr_enable_alerts
                )
            R.id.asr_enable_alerts ->
                showPopup(
                    yesNoOptions,
                    R.id.asr_enable_alerts_value, R.id.asr_enable_alerts
                )
            R.id.maghrib_enable_alerts ->
                showPopup(
                    yesNoOptions,
                    R.id.maghrib_enable_alerts_value, R.id.maghrib_enable_alerts
                )
            R.id.isha_enable_alerts ->
                showPopup(
                    yesNoOptions,
                    R.id.isha_enable_alerts_value, R.id.isha_enable_alerts
                )
        }

        val prayerOffsetIndex = prayerOffsetViews.indexOf(v?.id)

        if (prayerOffsetIndex != -1) {
            val enterOffsetFragment = EnterOffsetFragment()
            enterOffsetFragment.setListener(this, prayerOffsetIndex)
            enterOffsetFragment.arguments = Bundle().apply {
                putInt(
                    CURRENT_VALUE,
                    mViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.getOrNull(
                        prayerOffsetIndex
                    ) ?: 0
                )
            }
            enterOffsetFragment.show(requireActivity().supportFragmentManager, "$prayerOffsetIndex")
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val calculationMethodsStrings = resources.getStringArray(R.array.calculation_methods)
        val asrMethods = resources.getStringArray(R.array.asr_juristic_method)
        val latitudeMethods = resources.getStringArray(R.array.latitude_options)

        when (item?.groupId) {
            R.id.calculation_details -> {
                mViewModel.settingsValue?.calculationMethod =
                    CalculationMethod.values()[calculationMethodsStrings.indexOf(item.title.toString())]
            }
            R.id.asr_calculation_details -> {
                mViewModel.settingsValue?.asrCalculationMethod =
                    Madhab.values()[asrMethods.indexOf(item.title.toString())]
            }
            R.id.higher_latitude_details -> {
                mViewModel.settingsValue?.latitudeAdjustmentMethod =
                    latitudeMethods.indexOf(item.title.toString())
            }
            R.id.do_not_disturb -> {
                mViewModel.settingsValue?.doNotDisturbMinutes = parseInt(item.title.toString())
            }
            R.id.fajr_enable_alerts -> {
                val (yes) = yesNoOptions
                mViewModel.settingsValue?.generatePrayerTimeForPrayer?.set(
                    0, item.title.matches(yes.toRegex())
                )
            }
            R.id.dhuhr_enable_alerts -> {
                val (yes) = yesNoOptions
                mViewModel.settingsValue?.generatePrayerTimeForPrayer?.set(
                    1, item.title.matches(yes.toRegex())
                )
            }
            R.id.asr_enable_alerts -> {
                val (yes) = yesNoOptions
                mViewModel.settingsValue?.generatePrayerTimeForPrayer?.set(
                    2, item.title.matches(yes.toRegex())
                )
            }
            R.id.maghrib_enable_alerts -> {
                val (yes) = yesNoOptions
                mViewModel.settingsValue?.generatePrayerTimeForPrayer?.set(
                    3, item.title.matches(yes.toRegex())
                )
            }
            R.id.isha_enable_alerts -> {
                val (yes) = yesNoOptions
                mViewModel.settingsValue?.generatePrayerTimeForPrayer?.set(
                    4, item.title.matches(yes.toRegex())
                )
            }
        }
        updateSettings()
        return true
    }

    private fun updateSettings() {
        mViewModel.settingsValue?.let {
            mViewModel.updateSettings(it)
            mViewModel.isPrayerSettingsUpdated = true
        }
    }


    private fun isNotificationPolicyGranted(): Boolean {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
        }

        confirmationDialogFragment.show(requireActivity().supportFragmentManager, "dialog")
    }

    override fun onPause() {
        super.onPause()
        if (mViewModel.isPrayerSettingsUpdated)
            mViewModel.updatePrayerTimesData()
    }

    override fun onOffsetSave(offsetInMinutes: Int, index: Int) {
        val prayerTimeOffsetsInMinutes =
            mViewModel.settingsValue?.prayerTimeOffsetsInMinutes ?: return
        if (index in prayerTimeOffsetsInMinutes.indices) {
            mViewModel.settingsValue?.let {
                prayerTimeOffsetsInMinutes[index] = offsetInMinutes
                mViewModel.updateSettings(it)
                mViewModel.isPrayerSettingsUpdated = true
            }
        }
    }
}