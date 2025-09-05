package com.thesunnahrevival.sunnahassistant.views.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openDeveloperPage
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.others.AboutAppFragment
import com.thesunnahrevival.sunnahassistant.views.shareAppIntent
import com.thesunnahrevival.sunnahassistant.views.translateLink

abstract class MenuBarFragment : SunnahAssistantFragment(), MenuProvider {

    var mAppSettings: AppSettings? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            menu.findItem(R.id.dark_mode_switch).isVisible = false
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dark_mode_switch -> {
                if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
                val status = mAppSettings?.isLightMode ?: true
                mAppSettings?.isLightMode = !status
                mAppSettings?.let { mainActivityViewModel.updateSettings(it) }
                return true
            }
            R.id.settings -> {
                findNavController().navigate(R.id.settingsListFragment, null)
                return true
            }
            R.id.about -> {
                val fragment = AboutAppFragment()
                fragment.show(requireActivity().supportFragmentManager, "about")
                return true
            }
            R.id.share_app -> {
                val shareAppIntent = shareAppIntent()
                startActivity(
                    Intent.createChooser(
                        shareAppIntent,
                        getString(R.string.share_app)
                    )
                )
                return true
            }
            R.id.feedback -> {
                val intent = generateEmailIntent()
                if (intent.resolveActivity(requireActivity().packageManager) != null)
                    startActivity(intent)
                else
                    Toast.makeText(
                        context,
                        getString(R.string.no_email_app_installed),
                        Toast.LENGTH_LONG
                    ).show()
                return true
            }
            R.id.rate_this_app -> {
                context?.let { openPlayStore(it, "com.thesunnahrevival.sunnahassistant") }
                return true
            }
            R.id.help_translate_app -> {
                context?.let { translateLink(this) }
                return true
            }
            R.id.more_apps -> {
                context?.let { openDeveloperPage(it) }
                return true
            }
            R.id.oss_licenses -> {
                launchOSSLicensesActivity()
                return true
            }
            R.id.backup_restore -> {
                findNavController().navigate(R.id.backupRestoreFragment)
                return true
            }
            else -> return false
        }

    }

    open fun launchOSSLicensesActivity() {
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }

}