package com.thesunnahrevival.sunnahassistant.views.settings

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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.NotificationSettings
import com.thesunnahrevival.sunnahassistant.databinding.NotificationSettingsBinding
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService
import com.thesunnahrevival.sunnahassistant.utilities.createReminderNotificationChannel
import com.thesunnahrevival.sunnahassistant.utilities.deleteReminderNotificationChannel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

class NotificationSettingsFragment : SettingsFragmentWithPopups(), View.OnClickListener {

    private lateinit var mViewModel: SunnahAssistantViewModel
    private var isSettingsUpdated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: NotificationSettingsBinding = DataBindingUtil.inflate(
                inflater, R.layout.notification_settings, container, false)
        val myActivity = activity
        if (myActivity != null) {
            val options = resources.getStringArray(R.array.priority_options)
            mViewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            mViewModel.getSettings().observe(viewLifecycleOwner, Observer {
                if (it != null){
                    mViewModel.settingsValue = it
                    binding.settings = it
                    val toneName =
                            if (it.notificationToneUri.toString().isNotBlank())
                                RingtoneManager.getRingtone(context, it.notificationToneUri).getTitle(context)
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
                            })
                }
            })

            binding.notificationToneSettings.setOnClickListener(this)
            binding.notificationPrioritySettings.setOnClickListener(this)
            binding.notificationVibrationSettings.setOnClickListener(this)
            binding.nextReminderStickySettings.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed){
                    mViewModel.settingsValue?.showNextReminderNotification = isChecked
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                    requireContext().startService(Intent(requireContext(), NextReminderService::class.java))
                }
            }
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.notification_vibration_settings ->
                showPopup(resources.getStringArray(R.array.vibration_options),
                        R.id.vibration, R.id.notification_vibration_settings)
            R.id.notification_priority_settings ->
                showPopup(resources.getStringArray(R.array.priority_options),
                        R.id.importance, R.id.notification_priority_settings)
            R.id.notification_tone_settings -> {
                val currentRingtone: Uri? = mViewModel.settingsValue?.notificationToneUri
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtone)
                startActivityForResult(intent, 1)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        isSettingsUpdated = true
        return when(item?.groupId) {
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
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            mViewModel.settingsValue?.notificationToneUri =
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
            isSettingsUpdated = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (isSettingsUpdated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.let { deleteReminderNotificationChannel(it) }
                mViewModel.settingsValue?.let {
                    context?.let { it1 ->
                        createReminderNotificationChannel(
                                it1, it.notificationToneUri, it.isVibrate, it.priority)
                    }
                }
            }
        }
    }
}