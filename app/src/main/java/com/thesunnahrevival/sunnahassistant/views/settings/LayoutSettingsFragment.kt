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
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.databinding.FragmentLayoutSettingsBinding
import com.thesunnahrevival.sunnahassistant.views.FragmentWithPopups

class LayoutSettingsFragment : FragmentWithPopups(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding: FragmentLayoutSettingsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_layout_settings, container, false
        )
        binding.themeSettings.setOnClickListener(this)


        mainActivityViewModel.getSettings().observe(viewLifecycleOwner) {
            mainActivityViewModel.settingsValue = it
            binding.settings = it
            if (it != null) {
                binding.theme.text = getThemeLabel(it)
            }
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.theme_settings -> showPopup(
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    resources.getStringArray(R.array.theme_options_android_10_plus)
                } else {
                    resources.getStringArray(R.array.theme_options)
                },
                R.id.theme,
                R.id.theme_settings
            )
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.groupId) {
            R.id.theme_settings -> {
                val settings = mainActivityViewModel.settingsValue ?: return true
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    when (item.order) {
                        0 -> {
                            settings.themeMode = AppSettings.THEME_MODE_LIGHT
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }

                        1 -> {
                            settings.themeMode = AppSettings.THEME_MODE_DARK
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }

                        else -> {
                            settings.themeMode = AppSettings.THEME_MODE_FOLLOW_SYSTEM
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }
                } else {
                    if (item.order == 0) {
                        settings.themeMode = AppSettings.THEME_MODE_LIGHT
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    } else {
                        settings.themeMode = AppSettings.THEME_MODE_DARK
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                mainActivityViewModel.updateSettings(settings)
                true
            }

            else -> false
        }
    }

    private fun getThemeLabel(settings: AppSettings): String {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            when (settings.themeMode) {
                AppSettings.THEME_MODE_LIGHT -> {
                    getString(R.string.light_theme)
                }

                AppSettings.THEME_MODE_DARK -> {
                    getString(R.string.dark_theme)
                }

                else -> {
                    getString(R.string.theme_in_android_10_follows_system_settings)
                }
            }
        } else {
            when (settings.themeMode) {
                AppSettings.THEME_MODE_DARK -> {
                    getString(R.string.dark_theme)
                }

                else -> {
                    getString(R.string.light_theme)
                }
            }
        }
    }
}
