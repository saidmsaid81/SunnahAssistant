package com.thesunnahrevival.common.views.home

import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
//import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.utilities.generateEmailIntent
import com.thesunnahrevival.common.utilities.openDeveloperPage
import com.thesunnahrevival.common.utilities.openPlayStore
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SunnahAssistantFragment
import com.thesunnahrevival.common.views.others.AboutAppFragment
import com.thesunnahrevival.common.views.shareAppIntent
import com.thesunnahrevival.common.views.translateLink

abstract class MenuBarFragment : SunnahAssistantFragment(), PopupMenu.OnMenuItemClickListener {

    var mAppSettings: AppSettings? = null
    var categoryToDisplay = ""

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
                R.id.filter -> {
                    filterReminderByCategory()
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

    }

    private fun filterReminderByCategory() {
        val anchorView: View =
            (activity as MainActivity).findViewById<Toolbar>(R.id.toolbar).findViewById(R.id.filter)
        val popup = PopupMenu(context, anchorView)
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.filter_category)
        val displayAllMenuItem = popup.menu.add(
            R.id.category_display_filter, Menu.NONE, Menu.NONE, getString(R.string.display_all)
        )
            .setCheckable(true)
        if (categoryToDisplay.matches("".toRegex()))
            displayAllMenuItem.isChecked = true

        val categories = mAppSettings?.categories
        if (categories != null) {
            for (categoryTitle in categories) {
                    val categoryItem = popup.menu.add(
                            R.id.category_display_filter, Menu.NONE, Menu.NONE, categoryTitle
                    )
                    categoryItem.isCheckable = true
                    if (categoryToDisplay.matches(categoryTitle.toRegex()))
                        categoryItem.isChecked = true
            }
            popup.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        categoryToDisplay = if (!item.title.toString().matches(getString(R.string.display_all).toRegex()))
            item.title.toString()
        else
            ""
        item.isChecked = !item.isChecked
        filterData()
        return true
    }

    abstract fun filterData()
}