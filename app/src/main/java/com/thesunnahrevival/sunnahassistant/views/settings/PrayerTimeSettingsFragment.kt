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
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentPrayerTimeSettingsBinding
import com.thesunnahrevival.sunnahassistant.views.FragmentWithPopups
import com.thesunnahrevival.sunnahassistant.views.dialogs.ConfirmationDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterLocationDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterOffsetFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterOffsetFragment.Companion.CURRENT_VALUE
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
        val binding: FragmentPrayerTimeSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_prayer_time_settings, container, false
        )

        mViewModel.isPrayerSettingsUpdated = false
        binding.prayers = resources.getStringArray(R.array.prayer_names)
        binding.offsetOptions = resources.getStringArray(R.array.offset_options)
        binding.hoursLabel = getString(R.string.hours)
        binding.minutesLabel = getString(R.string.minutes)
        binding.onTimeLabel = resources.getStringArray(R.array.notify_options)[1]

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
                    settingsValue.generatePrayerToDosAfter =
                        Date(System.currentTimeMillis() - 86400000)
                updateSettings()
            }
        }

        setOnClickListeners(binding)
        return binding.root
    }

    private fun setOnClickListeners(binding: FragmentPrayerTimeSettingsBinding) {
        yesNoOptions[0] = getString(R.string.yes)
        yesNoOptions[1] = getString(R.string.no)

        binding.locationDetails.setOnClickListener {
            val dialogFragment = EnterLocationDialogFragment()
            dialogFragment.show(requireActivity().supportFragmentManager, "dialog")
        }
        binding.calculationDetails.setOnClickListener {
            showPopup(
                resources.getStringArray(R.array.calculation_methods),
                R.id.calculation_method, it.id
            )
        }
        binding.asrCalculationDetails.setOnClickListener {
            showPopup(
                resources.getStringArray(R.array.asr_juristic_method),
                R.id.asr_calculation_method, it.id
            )
        }
        binding.higherLatitudeDetails.setOnClickListener {
            showPopup(
                resources.getStringArray(R.array.latitude_options),
                R.id.higher_latitude_method, it.id
            )
        }
        binding.doNotDisturb.setOnClickListener {
            if (isNotificationPolicyGranted())
                showPopup(
                    resources.getStringArray(R.array.do_not_disturb_minutes),
                    R.id.do_not_disturb_minutes, R.id.do_not_disturb
                )
        }

        binding.fajrNotificationSettings.setOnClickListener(this)
        binding.dhuhrNotificationSettings.setOnClickListener(this)
        binding.asrNotificationSettings.setOnClickListener(this)
        binding.maghribNotificationSettings.setOnClickListener(this)
        binding.ishaNotificationSettings.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            val notifyOptions = resources.getStringArray(R.array.notify_options)
            showPopup(
                notifyOptions,
                view.id, view.id
            )
        }
    }

    private fun showEnterOffsetFragment(prayerOffsetIndex: Int) {
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
        enterOffsetFragment.show(
            requireActivity().supportFragmentManager,
            "$prayerOffsetIndex"
        )
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
            R.id.fajr_notification_settings -> setPrayerOffset(0, item)
            R.id.dhuhr_notification_settings -> setPrayerOffset(1, item)
            R.id.asr_notification_settings -> setPrayerOffset(2, item)
            R.id.maghrib_notification_settings -> setPrayerOffset(3, item)
            R.id.isha_notification_settings -> setPrayerOffset(4, item)
        }
        updateSettings()
        return true
    }

    private fun setPrayerOffset(prayerTimeIndex: Int, item: MenuItem) {
        val notifyOptions = resources.getStringArray(R.array.notify_options)
        mViewModel.settingsValue?.enablePrayerTimeAlertsFor?.set(prayerTimeIndex, true)

        when (notifyOptions.indexOf(item.title)) {
            0 -> mViewModel.settingsValue?.enablePrayerTimeAlertsFor?.set(prayerTimeIndex, false)
            1 -> mViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, 0)
            2 -> mViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -5)
            3 -> mViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -15)
            4 -> mViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -30)
            else -> showEnterOffsetFragment(prayerTimeIndex)
        }
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