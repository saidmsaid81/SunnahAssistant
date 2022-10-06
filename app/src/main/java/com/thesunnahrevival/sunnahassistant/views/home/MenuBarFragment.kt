package com.thesunnahrevival.sunnahassistant.views.home

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openDeveloperPage
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.AboutAppFragment
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.shareAppIntent
import com.thesunnahrevival.sunnahassistant.views.translateLink

abstract class MenuBarFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    var mAppSettings: AppSettings? = null
    var categoryToDisplay = ""
    lateinit var mViewModel: SunnahAssistantViewModel

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            menu.findItem(R.id.dark_mode_switch).isVisible = false
        }
        if (mAppSettings?.isExpandedLayout == false)
            menu.findItem(R.id.layout).title = requireContext().getString(R.string.default_display_of_data)
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
                R.id.layout -> {
                    val status = mAppSettings?.isExpandedLayout ?: true
                    mAppSettings?.isExpandedLayout = !status
                    mAppSettings?.let { mViewModel.updateSettings(it) }
                    startActivity(Intent(context, MainActivity::class.java))
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
                    startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    return true
                }
                R.id.backup_restore -> {
                    findNavController().navigate(R.id.backupRestoreFragment)
                    return true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun filterReminderByCategory() {
        val anchorView: View = (activity as MainActivity).findViewById<Toolbar>(R.id.toolbar).findViewById(R.id.filter)
        val popup = PopupMenu(context, anchorView)
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.filter_category)
        val displayAllMenuItem = popup.menu.add(
                R.id.category_display_filter, Menu.NONE, Menu.NONE, getString(R.string.display_all))
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