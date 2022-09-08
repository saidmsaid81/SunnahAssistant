package com.thesunnahrevival.common.views.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.databinding.DisplaySettingsBinding
import com.thesunnahrevival.common.views.FragmentWithPopups

class LayoutSettingsFragment : FragmentWithPopups(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: DisplaySettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.display_settings, container, false
        )
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
            else -> false
        }
    }
}