package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
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
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterLocationDialogFragment

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
        }
        mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
        return true
    }

    override fun onPause() {
        super.onPause()
        mViewModel.updatePrayerTimesData()
    }
}