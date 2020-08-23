package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.InstallStatus
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity(){
    private lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        activity = this
        getSettings()

    }

    private fun getSettings() {
        val viewModel = ViewModelProviders.of(this).get(SunnahAssistantViewModel::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = viewModel.getAppSettingsValue()
            var numberOfLaunches = settings?.numberOfLaunches
            if (numberOfLaunches != null){
                numberOfLaunches++
                settings?.numberOfLaunches = numberOfLaunches
                settings?.let { viewModel.updateSettings(it) }
            }

            withContext(Dispatchers.Main){
                //Set theme
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) //Android 9 and below
                    if (settings?.isLightMode != false)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //Dark Mode

                if ((settings?.numberOfLaunches ?: 0) > 0 && (settings?.numberOfLaunches ?: 0) % 7 == 0){
                        showInAppReviewPrompt(activity)
                }
                else if ((settings?.numberOfLaunches ?: 0) > 0 && (settings?.numberOfLaunches ?: 0) % 3 == 0){
                    checkForUpdates(activity)
                }

                else if ((settings?.numberOfLaunches ?: 0) == 5 && !supportedLocales.contains(Locale.getDefault().language)){
                    val listener = View.OnClickListener {
                        val browserIntent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://crwd.in/sunnah-assistant"))
                        if (browserIntent.resolveActivity(packageManager) != null) {
                            startActivity(browserIntent);
                        }
                    }

                    popupSnackbar(activity,
                            getString(R.string.help_translate_app, Locale.getDefault().displayLanguage),
                            10000,
                            getString(R.string.translate),
                            listener)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }

    // Checks that the in app update is not stalled during 'onResume()'.
    override fun onResume() {
        super.onResume()
        // Creates instance of the manager.
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbar(activity, getString(R.string.update_downloaded),  Snackbar.LENGTH_INDEFINITE,
                                getString(R.string.restart), View.OnClickListener { appUpdateManager.completeUpdate() } )
                    }
                }
    }
}