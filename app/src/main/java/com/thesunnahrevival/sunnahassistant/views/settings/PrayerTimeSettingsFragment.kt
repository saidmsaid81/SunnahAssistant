package com.thesunnahrevival.sunnahassistant.views.settings

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.databinding.FragmentPrayerTimeSettingsBinding
import com.thesunnahrevival.sunnahassistant.utilities.REQUEST_NOTIFICATION_PERMISSION_CODE
import com.thesunnahrevival.sunnahassistant.utilities.extractNumber
import com.thesunnahrevival.sunnahassistant.views.FragmentWithPopups
import com.thesunnahrevival.sunnahassistant.views.dialogs.ConfirmationDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterLocationDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterOffsetFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterOffsetFragment.Companion.CURRENT_VALUE
import com.thesunnahrevival.sunnahassistant.views.dialogs.TimePickerFragment
import java.util.Date

open class PrayerTimeSettingsFragment : FragmentWithPopups(), View.OnClickListener,
    EnterOffsetFragment.EnterOffsetFragmentListener,
    EnterLocationDialogFragment.EnterLocationDialogListener,
    TimePickerFragment.OnTimeSetListener {

    private val yesNoOptions = arrayOf("", "")
    private var pendingPrayerAlertsActivation = false
    private var pendingCustomTimePrayerIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentPrayerTimeSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_prayer_time_settings, container, false
        )

        mainActivityViewModel.isPrayerSettingsUpdated = false
        binding.prayers = resources.getStringArray(R.array.prayer_names)
        binding.offsetOptions = resources.getStringArray(R.array.offset_options)
        binding.hoursLabel = getString(R.string.hours)
        binding.minutesLabel = getString(R.string.minutes)
        binding.onTimeLabel = resources.getStringArray(R.array.notify_options)[1]
        binding.timeModeOptions = resources.getStringArray(R.array.prayer_time_mode_options)

        mainActivityViewModel.getSettings().observe(viewLifecycleOwner) {
            if (it != null) {
                mainActivityViewModel.settingsValue = it
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
                val settingsValue = mainActivityViewModel.settingsValue ?: return@setOnCheckedChangeListener

                if (isChecked && settingsValue.formattedAddress.isNullOrBlank()) {
                    // Location not set - show dialog first
                    pendingPrayerAlertsActivation = true
                    binding.activatePrayerTimeAlerts.isChecked = false
                    val dialogFragment = EnterLocationDialogFragment()
                    dialogFragment.setListener(this)
                    dialogFragment.show(requireActivity().supportFragmentManager, "location_dialog")
                    return@setOnCheckedChangeListener
                }

                activatePrayerTimeAlerts(settingsValue, isChecked)
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

        binding.fajrTimeModeSettings.setOnClickListener(this)
        binding.dhuhrTimeModeSettings.setOnClickListener(this)
        binding.asrTimeModeSettings.setOnClickListener(this)
        binding.maghribTimeModeSettings.setOnClickListener(this)
        binding.ishaTimeModeSettings.setOnClickListener(this)

        binding.fajrCustomTimeSettings.setOnClickListener(this)
        binding.dhuhrCustomTimeSettings.setOnClickListener(this)
        binding.asrCustomTimeSettings.setOnClickListener(this)
        binding.maghribCustomTimeSettings.setOnClickListener(this)
        binding.ishaCustomTimeSettings.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view == null) {
            return
        }
        when (view.id) {
            R.id.fajr_time_mode_settings -> showTimeModePopup(0, view.id)
            R.id.dhuhr_time_mode_settings -> showTimeModePopup(1, view.id)
            R.id.asr_time_mode_settings -> showTimeModePopup(2, view.id)
            R.id.maghrib_time_mode_settings -> showTimeModePopup(3, view.id)
            R.id.isha_time_mode_settings -> showTimeModePopup(4, view.id)
            R.id.fajr_custom_time_settings -> showCustomTimePicker(0)
            R.id.dhuhr_custom_time_settings -> showCustomTimePicker(1)
            R.id.asr_custom_time_settings -> showCustomTimePicker(2)
            R.id.maghrib_custom_time_settings -> showCustomTimePicker(3)
            R.id.isha_custom_time_settings -> showCustomTimePicker(4)
            else -> {
                val notifyOptions = resources.getStringArray(R.array.notify_options)
                showPopup(notifyOptions, view.id, view.id)
            }
        }
    }

    private fun showTimeModePopup(prayerIndex: Int, viewId: Int) {
        pendingCustomTimePrayerIndex = prayerIndex
        val timeModeOptions = resources.getStringArray(R.array.prayer_time_mode_options)
        showPopup(timeModeOptions, viewId, viewId)
    }

    private fun showCustomTimePicker(prayerIndex: Int) {
        pendingCustomTimePrayerIndex = prayerIndex
        val currentTime = getCustomTimeForPrayer(prayerIndex)
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.arguments = Bundle().apply {
            putString(TimePickerFragment.TIMESET, currentTime)
        }
        timePickerFragment.setListener(this)
        timePickerFragment.show(requireActivity().supportFragmentManager, "customTimePicker")
    }

    private fun getCustomTimeForPrayer(prayerIndex: Int): String? {
        return when (prayerIndex) {
            0 -> mainActivityViewModel.settingsValue?.fajrCustomTime
            1 -> mainActivityViewModel.settingsValue?.dhuhrCustomTime
            2 -> mainActivityViewModel.settingsValue?.asrCustomTime
            3 -> mainActivityViewModel.settingsValue?.maghribCustomTime
            4 -> mainActivityViewModel.settingsValue?.ishaCustomTime
            else -> null
        }
    }

    override fun onTimeSet(timeString: String) {
        if (pendingCustomTimePrayerIndex >= 0) {
            val timeIn24Hour = convertTo24HourFormat(timeString)
            setCustomTimeForPrayer(pendingCustomTimePrayerIndex, timeIn24Hour)
            pendingCustomTimePrayerIndex = -1
            updateSettings()
        }
    }

    private fun convertTo24HourFormat(timeString: String): String {
        return try {
            if (timeString.contains("am", ignoreCase = true) || timeString.contains("pm", ignoreCase = true)) {
                val inputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.ENGLISH)
                val date = inputFormat.parse(timeString)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    timeString
                }
            } else {
                timeString
            }
        } catch (_: Exception) {
            timeString.replace(" am", "").replace(" pm", "").replace(" AM", "").replace(" PM", "")
        }
    }

    private fun setCustomTimeForPrayer(prayerIndex: Int, time: String?) {
        when (prayerIndex) {
            0 -> mainActivityViewModel.settingsValue?.fajrCustomTime = time
            1 -> mainActivityViewModel.settingsValue?.dhuhrCustomTime = time
            2 -> mainActivityViewModel.settingsValue?.asrCustomTime = time
            3 -> mainActivityViewModel.settingsValue?.maghribCustomTime = time
            4 -> mainActivityViewModel.settingsValue?.ishaCustomTime = time
        }
    }

    private fun showEnterOffsetFragment(prayerOffsetIndex: Int) {
        val enterOffsetFragment = EnterOffsetFragment()
        enterOffsetFragment.setListener(this, prayerOffsetIndex)
        enterOffsetFragment.arguments = Bundle().apply {
            putInt(
                CURRENT_VALUE,
                mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.getOrNull(
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
                mainActivityViewModel.settingsValue?.calculationMethod =
                    CalculationMethod.values()[calculationMethodsStrings.indexOf(item.title.toString())]
            }
            R.id.asr_calculation_details -> {
                mainActivityViewModel.settingsValue?.asrCalculationMethod =
                    Madhab.values()[asrMethods.indexOf(item.title.toString())]
            }
            R.id.higher_latitude_details -> {
                mainActivityViewModel.settingsValue?.latitudeAdjustmentMethod =
                    latitudeMethods.indexOf(item.title.toString())
            }
            R.id.do_not_disturb -> {
                mainActivityViewModel.settingsValue?.doNotDisturbMinutes = item.title?.extractNumber() ?: 0
            }
            R.id.fajr_notification_settings -> setPrayerOffset(0, item)
            R.id.dhuhr_notification_settings -> setPrayerOffset(1, item)
            R.id.asr_notification_settings -> setPrayerOffset(2, item)
            R.id.maghrib_notification_settings -> setPrayerOffset(3, item)
            R.id.isha_notification_settings -> setPrayerOffset(4, item)
            R.id.fajr_time_mode_settings,
            R.id.dhuhr_time_mode_settings,
            R.id.asr_time_mode_settings,
            R.id.maghrib_time_mode_settings,
            R.id.isha_time_mode_settings -> {
                handleTimeModeSelection(item)
            }
        }
        updateSettings()
        return true
    }

    private fun handleTimeModeSelection(item: MenuItem) {
        val timeModeOptions = resources.getStringArray(R.array.prayer_time_mode_options)
        val selectedIndex = timeModeOptions.indexOf(item.title.toString())
        if (selectedIndex == 0) {
            setCustomTimeForPrayer(pendingCustomTimePrayerIndex, null)
        } else if (selectedIndex == 1) {
            showCustomTimePicker(pendingCustomTimePrayerIndex)
        }
    }

    private fun setPrayerOffset(prayerTimeIndex: Int, item: MenuItem) {
        val notifyOptions = resources.getStringArray(R.array.notify_options)
        mainActivityViewModel.settingsValue?.enablePrayerTimeAlertsFor?.set(prayerTimeIndex, true)

        when (notifyOptions.indexOf(item.title)) {
            0 -> mainActivityViewModel.settingsValue?.enablePrayerTimeAlertsFor?.set(prayerTimeIndex, false)
            1 -> mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, 0)
            2 -> mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -5)
            3 -> mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -15)
            4 -> mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes?.set(prayerTimeIndex, -30)
            else -> showEnterOffsetFragment(prayerTimeIndex)
        }
    }

    private fun updateSettings() {
        mainActivityViewModel.settingsValue?.let {
            mainActivityViewModel.updateSettings(it)
            mainActivityViewModel.isPrayerSettingsUpdated = true
        }
    }

    private fun activatePrayerTimeAlerts(settingsValue: AppSettings, isChecked: Boolean) {
        settingsValue.isAutomaticPrayerAlertsEnabled = isChecked
        if (isChecked) {
            settingsValue.generatePrayerToDosAfter =
                Date(System.currentTimeMillis() - 86400000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                mainActivityViewModel.incrementNotificationPermissionRequestsCount()
                requireActivity().requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION_CODE
                )
            }
        }
        updateSettings()
    }

    override fun onLocationSaved() {
        if (pendingPrayerAlertsActivation) {
            pendingPrayerAlertsActivation = false
            mainActivityViewModel.settingsValue?.let { settingsValue ->
                activatePrayerTimeAlerts(settingsValue, true)
            }
        }
    }

    override fun onLocationDialogCancelled() {
        pendingPrayerAlertsActivation = false
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
        if (mainActivityViewModel.isPrayerSettingsUpdated)
            mainActivityViewModel.updatePrayerTimesData()
    }

    override fun onOffsetSave(offsetInMinutes: Int, index: Int) {
        val prayerTimeOffsetsInMinutes =
            mainActivityViewModel.settingsValue?.prayerTimeOffsetsInMinutes ?: return
        if (index in prayerTimeOffsetsInMinutes.indices) {
            mainActivityViewModel.settingsValue?.let {
                prayerTimeOffsetsInMinutes[index] = offsetInMinutes
                mainActivityViewModel.updateSettings(it)
                mainActivityViewModel.isPrayerSettingsUpdated = true
            }
        }
    }
}