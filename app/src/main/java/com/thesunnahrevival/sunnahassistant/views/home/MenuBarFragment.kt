package com.thesunnahrevival.sunnahassistant.views.home

import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openDeveloperPage
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.others.AboutAppFragment
import com.thesunnahrevival.sunnahassistant.views.shareAppIntent
import com.thesunnahrevival.sunnahassistant.views.translateLink

abstract class MenuBarFragment : SunnahAssistantFragment() {

    var mAppSettings: AppSettings? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            menu.findItem(R.id.dark_mode_switch).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myActivity = activity
        if (myActivity != null) {
            when (item.itemId) {
                R.id.dark_mode_switch -> {
                    if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_NO) {
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                    }
                    val status = mAppSettings?.isLightMode ?: true
                    mAppSettings?.isLightMode = !status
                    mAppSettings?.let { mViewModel.updateSettings(it) }
                    return true
                }
                R.id.settings -> {
                    findNavController().navigate(R.id.settingsListFragment, null)
                    return true
                }
                R.id.about -> {
                    val fragment = AboutAppFragment()
                    fragment.show(myActivity.supportFragmentManager, "about")
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
                    if (intent.resolveActivity(myActivity.packageManager) != null)
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
            }
        }

        return super.onOptionsItemSelected(item)
    }

    open fun launchOSSLicensesActivity() {
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }

}