package com.thesunnahrevival. sunnahassistant.views

import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.BR
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.NotificationSettings
import com.thesunnahrevival.sunnahassistant.databinding.*
import com.thesunnahrevival.sunnahassistant.utilities.NotificationUtil
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.CategoriesSettingsAdapter

class SettingsFragment : SettingsFragmentListeners() {

    companion object {
        @JvmStatic
        fun newInstance(id: Int): SettingsFragment {
            val args = Bundle()
            args.putInt("id", id)
            val settingsFragment = SettingsFragment()
            settingsFragment.arguments = args
            return settingsFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(
                inflater, arguments?.getInt("id") ?: R.layout.settings_lists, container, false)

        val myActivity = activity
        if (myActivity != null){
            mViewModel = ViewModelProviders.of(myActivity).get(SettingsViewModel::class.java)
            mViewModel.settings.observe(viewLifecycleOwner, Observer { settings: AppSettings? ->
                if (settings != null) {
                    mBinding.setVariable(BR.settings, settings)
                    setCustomLayoutSettings(settings)
                }
            })
            customizeLayout(mBinding)
        }
        return mBinding.root
    }

    private fun setCustomLayoutSettings(settings: AppSettings) {
        when (mBinding) {
            is PrayerTimeSettingsBinding -> setCustomPrayerSettings(settings)

            is CategoriesSettingsBinding ->
                (mBinding as CategoriesSettingsBinding).categoriesList.adapter?.notifyDataSetChanged()

            is NotificationSettingsBinding -> {
                if (settings.notificationToneUri.toString().isBlank()) {
                    mViewModel.updateRingtone(RingtoneManager.getActualDefaultRingtoneUri(
                            context, RingtoneManager.TYPE_NOTIFICATION))
                }
                setCustomNotificationSettings(settings)
            }
        }
    }

    private fun setCustomNotificationSettings(settings: AppSettings) {
        val options = resources.getStringArray(R.array.priority_options)
        (mBinding as NotificationSettingsBinding).notificationSettings =
                NotificationSettings(
                        RingtoneManager.getRingtone(context, settings.notificationToneUri).getTitle(context),
                        settings.isVibrate,
                        when (settings.priority) {
                            NotificationManager.IMPORTANCE_HIGH -> options[3]
                            NotificationManager.IMPORTANCE_DEFAULT -> options[2]
                            NotificationManager.IMPORTANCE_LOW -> options[1]
                            NotificationManager.IMPORTANCE_MIN -> options[0]
                            else -> options[2]
                        })
    }

    private fun setCustomPrayerSettings(settings: AppSettings) {
        (mBinding as PrayerTimeSettingsBinding).setCalculationMethod(
                resources.getStringArray(R.array.calculation_methods)[settings.method])
        (mBinding as PrayerTimeSettingsBinding).setAsrCalculationMethod(
                resources.getStringArray(R.array.asr_juristic_method)[settings.asrCalculationMethod])
        (mBinding as PrayerTimeSettingsBinding).latitudeAdjustmentMethod =
                resources.getStringArray(R.array.latitude_options)[settings.latitudeAdjustmentMethod]

    }

    private fun customizeLayout(binding: ViewDataBinding) {
        when (binding) {
            is SettingsListsBinding -> {
                binding.version.text =
                        String.format(getString(R.string.version), BuildConfig.VERSION_NAME)
                context?.let {
                    val adapter = ArrayAdapter<String>(
                            it, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.settings_lists))
                    binding.settingsLists.adapter = adapter
                    binding.settingsLists.onItemClickListener = this
                }
            }
            is HijriDateSettingsBinding -> {
                binding.changeHijriOffset.setOnClickListener(this)
                binding.displayHijriDateSwitch.setOnCheckedChangeListener(this)
            }
            is PrayerTimeSettingsBinding -> {
                binding.activatePrayerTimeAlerts.setOnCheckedChangeListener(this)
                binding.locationDetails.setOnClickListener(this)
                binding.calculationDetails.setOnClickListener(this)
                binding.asrCalculationDetails.setOnClickListener(this)
                binding.higherLatitudeDetails.setOnClickListener(this)
            }
            is DisplaySettingsBinding -> {
                binding.layoutSettings.setOnClickListener(this)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                    binding.themeSettings.setOnClickListener(this)
            }
            is CategoriesSettingsBinding -> {
                binding.categoriesList.adapter = mViewModel.settings.value?.categories?.let {
                    CategoriesSettingsAdapter(it, this) }
                binding.fab.setOnClickListener(this)
            }
            is NotificationSettingsBinding -> {
                binding.nextReminderStickySettings.setOnCheckedChangeListener(this)
                val reminderNotificationChannel = NotificationUtil.getReminderNotificationChannel(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && reminderNotificationChannel != null) {
                    mViewModel.updateNotificationSettings(
                            reminderNotificationChannel.sound,
                            reminderNotificationChannel.shouldVibrate(),
                            reminderNotificationChannel.importance)
                }
                binding.notificationToneSettings.setOnClickListener(this)
                binding.notificationVibrationSettings.setOnClickListener(this)
                binding.notificationPrioritySettings.setOnClickListener(this)

            }
        }
    }
}
