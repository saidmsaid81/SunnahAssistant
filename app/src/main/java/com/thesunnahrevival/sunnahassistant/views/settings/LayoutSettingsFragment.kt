package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.DisplaySettingsBinding

class LayoutSettingsFragment : SettingsFragmentWithPopups(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: DisplaySettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.display_settings, container, false
        )
        binding.layoutSettings.setOnClickListener(this)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            binding.themeSettings.setOnClickListener(this)


        mViewModel.getSettings().observe(viewLifecycleOwner) {
            mViewModel.settingsValue = it
            binding.settings = it
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.layout_settings -> showPopup(
                resources.getStringArray(R.array.layout_options),
                R.id.layout, R.id.layout_settings
            )
            R.id.theme_settings -> showPopup(
                resources.getStringArray(R.array.theme_options),
                R.id.theme,
                R.id.theme_settings
            )
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.groupId) {
            R.id.theme_settings -> {
                if (item.title.toString().matches("Light".toRegex())) {
                    mViewModel.settingsValue?.isLightMode = true
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    mViewModel.settingsValue?.isLightMode = false
                    mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }
            R.id.layout_settings -> {
                mViewModel.settingsValue?.isExpandedLayout =
                    (item.title.toString().matches("Expanded View".toRegex()))
                mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
                true
            }
            else -> false
        }
    }
}