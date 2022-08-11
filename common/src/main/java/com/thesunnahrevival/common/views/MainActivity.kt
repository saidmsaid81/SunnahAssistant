package com.thesunnahrevival.common.views

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.utilities.createNotificationChannels
import com.thesunnahrevival.common.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.common.views.home.TodayFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


open class MainActivity : AppCompatActivity() {

    private lateinit var activity: MainActivity
    lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannels(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        activity = this
        firebaseAnalytics = Firebase.analytics
        getSettings()
        if (intent.extras?.get("link") != null)
            findNavController(R.id.myNavHostFragment).navigate(R.id.webviewFragment, intent.extras)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todayFragment -> bottom_navigation_view.visibility = View.VISIBLE
                R.id.calendarFragment -> {
                    bottom_navigation_view.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                else -> bottom_navigation_view.visibility = View.GONE
            }
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.today -> findNavController(R.id.myNavHostFragment).navigate(R.id.todayFragment)
                R.id.calendar ->
                    findNavController(R.id.myNavHostFragment).navigate(R.id.calendarFragment)
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun getSettings() {
        val viewModel = ViewModelProvider(this).get(SunnahAssistantViewModel::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = viewModel.getAppSettingsValue()
            var numberOfLaunches = settings?.numberOfLaunches
            if (numberOfLaunches != null) {
                numberOfLaunches++
                settings?.numberOfLaunches = numberOfLaunches
                settings?.let { viewModel.updateSettings(it) }

            }

            withContext(Dispatchers.Main) {
                if (settings != null) {
                    applySettings(settings)
                }
            }
        }
    }

    private fun applySettings(settings: AppSettings) {
        //Set theme
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) //Android 9 and below
            if (settings.isLightMode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //Dark Mode

        if (settings.numberOfLaunches > 0 && settings.numberOfLaunches % 3 == 0) {
            checkForUpdates(activity)
        } else if (settings.numberOfLaunches > 0 && settings.numberOfLaunches % 5 == 0) {
            val random = Random.nextInt(1, 5)
            //Work-around to get the active fragment
            val navHostFragment: Fragment? = supportFragmentManager
                .findFragmentById(R.id.myNavHostFragment)
            val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

            if (fragment is TodayFragment) {
                when (random) {
                    1 -> {
                        showHelpTranslateSnackBar(fragment)
                    }
                    2 -> {
                        showSendFeedbackSnackBar(fragment)
                    }
                    3 -> {
                        showShareAppSnackBar(fragment)
                    }
                    4 -> {
                        showInAppReviewPrompt(activity)
                    }
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.myNavHostFragment)
        val homeFragments = listOf(R.id.todayFragment, R.id.calendarFragment)
        if (!homeFragments.contains(navController.currentDestination?.id))
            return navController.navigateUp()
        else
            finish()
        return true
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
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
                    popupSnackbar(
                        activity, getString(R.string.update_downloaded), Snackbar.LENGTH_INDEFINITE,
                        getString(R.string.restart)
                    ) { appUpdateManager.completeUpdate() }
                }
            }
    }
}