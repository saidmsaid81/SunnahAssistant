package com.thesunnahrevival.common.views.settings

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.NotificationSettings
import com.thesunnahrevival.common.databinding.NotificationSettingsBinding
import com.thesunnahrevival.common.utilities.NextReminderService
import com.thesunnahrevival.common.utilities.createReminderNotificationChannel
import com.thesunnahrevival.common.utilities.deleteReminderNotificationChannel

class NotificationSettingsFragment : SettingsFragmentWithPopups(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private var isSettingsUpdated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: NotificationSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.notification_settings, container, false
        )

        val options = resources.getStringArray(R.array.priority_options)
        mViewModel.getSettings().observe(viewLifecycleOwner) {
            if (it != null) {
                mViewModel.settingsValue = it
                binding.settings = it
                val toneName =
                    if (it.notificationToneUri.toString().isNotBlank())
                        RingtoneManager.getRingtone(context, it.notificationToneUri)
                            .getTitle(context)
                    else
                        getString(R.string.unavialable)
                binding.notificationSettings = NotificationSettings(
                    toneName,
                    it.isVibrate,
                    when (it.priority) {
                        NotificationManager.IMPORTANCE_HIGH -> options[3]
                        NotificationManager.IMPORTANCE_DEFAULT -> options[2]
                        NotificationManager.IMPORTANCE_LOW -> options[1]
                        NotificationManager.IMPORTANCE_MIN -> options[0]
                        else -> options[2]
                    }
                )
            }
        }

        binding.notificationToneSettings.setOnClickListener(this)
        binding.notificationPrioritySettings.setOnClickListener(this)
        binding.notificationVibrationSettings.setOnClickListener(this)
        binding.nextReminderStickySettings.setOnCheckedChangeListener(this)
        binding.useReliableAlarms.setOnCheckedChangeListener(this)

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.notification_vibration_settings ->
                showPopup(
                    resources.getStringArray(R.array.vibration_options),
                    R.id.vibration, R.id.notification_vibration_settings
                )
            R.id.notification_priority_settings ->
                showPopup(
                    resources.getStringArray(R.array.priority_options),
                    R.id.importance, R.id.notification_priority_settings
                )
            R.id.notification_tone_settings -> {
                val currentRingtone: Uri? = mViewModel.settingsValue?.notificationToneUri
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_NOTIFICATION
                )
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtone)
                startActivityForResult(intent, 1)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        isSettingsUpdated = true
        return when (item?.groupId) {
            R.id.notification_vibration_settings -> {
                mViewModel.settingsValue?.isVibrate = item.title.toString().matches("On".toRegex())
                mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                true
            }
            R.id.notification_priority_settings -> {
                mViewModel.settingsValue?.priority = item.order + 1
                mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            mViewModel.settingsValue?.notificationToneUri =
                data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
            isSettingsUpdated = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSettingsUpdated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.let { deleteReminderNotificationChannel(it) }
                mViewModel.settingsValue?.let {
                    context?.let { it1 ->
                        createReminderNotificationChannel(
                            it1, it.notificationToneUri, it.isVibrate, it.priority
                        )
                    }
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView?.isPressed == true) {
            if (buttonView.id == R.id.next_reminder_sticky_settings)
                mViewModel.settingsValue?.showNextReminderNotification = isChecked
            else if (buttonView.id == R.id.use_reliable_alarms)
                mViewModel.settingsValue?.useReliableAlarms = isChecked
            mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
            requireContext().startService(Intent(requireContext(), NextReminderService::class.java))
        }
    }
}