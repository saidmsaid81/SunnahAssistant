package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.services.NextToDoService
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.createNotificationChannels
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.home.CalendarFragment
import com.thesunnahrevival.sunnahassistant.views.home.TodayFragment
import com.thesunnahrevival.sunnahassistant.views.others.WelcomeFragment
import com.thesunnahrevival.sunnahassistant.views.toDoDetails.ResolveMalformedToDosFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random


const val requestCodeForUpdate: Int = 1

open class MainActivity : AppCompatActivity() {

    private lateinit var activity: MainActivity
    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var mAdView: AdView

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

        startService(Intent(this, NextToDoService::class.java))

        loadAds()

        val link = intent.extras?.get("link")
        if (link != null) {
            if ((link as String).contains("market://details")) {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_VIEW
                sendIntent.data = Uri.parse(link)
                if (sendIntent.resolveActivity(packageManager) != null) {
                    startActivity(sendIntent)
                }
            } else
                InAppBrowser(this, lifecycleScope).launchInAppBrowser(
                    link,
                    findNavController(R.id.myNavHostFragment)
                )
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            mAdView.visibility = View.VISIBLE
            when (destination.id) {
                R.id.todayFragment -> bottom_navigation_view.visibility = View.VISIBLE
                R.id.calendarFragment, R.id.tipsFragment -> {
                    bottom_navigation_view.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                R.id.welcomeFragment, R.id.resolveMalformedToDosFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottom_navigation_view.visibility = View.GONE
                    mAdView.visibility = View.GONE
                }
                R.id.changelogFragment -> mAdView.visibility = View.GONE
                else -> bottom_navigation_view.visibility = View.GONE
            }
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.today -> findNavController(R.id.myNavHostFragment).navigate(R.id.todayFragment)
                R.id.calendar ->
                    findNavController(R.id.myNavHostFragment).navigate(R.id.calendarFragment)
                R.id.tips ->
                    findNavController(R.id.myNavHostFragment).navigate(R.id.tipsFragment)
            }
            return@setOnNavigationItemSelectedListener true
        }
    }


    private fun loadAds() {
        MobileAds.initialize(this) { }
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
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
                    if (!settings.language.matches(Locale.getDefault().language.toRegex())) {
                        viewModel.localeUpdate()
                    }
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
            val appUpdateManager = AppUpdateManagerFactory.create(this)

            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    // Request the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        requestCodeForUpdate
                    )

                }
            }

            appUpdateManager.registerListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbar(
                        activity,
                        activity.getString(R.string.update_downloaded),
                        Snackbar.LENGTH_INDEFINITE,
                        activity.getString(R.string.restart)
                    ) { appUpdateManager.completeUpdate() }
                }
            }

        } else if (settings.numberOfLaunches > 0 && settings.numberOfLaunches % 5 == 0) {
            val random = Random(System.currentTimeMillis()).nextInt(1, 5)
            val fragment = getActiveFragment()

            if (fragment is TodayFragment && fragment !is CalendarFragment) {
                when (random) {
                    1 -> {
                        if (!supportedLocales.contains(Locale.getDefault().language))
                            showHelpTranslateBanner(fragment)
                        else
                            showShareAppBanner(fragment)
                    }
                    2 -> {
                        showSendFeedbackBanner(fragment)
                    }
                    3 -> {
                        showShareAppBanner(fragment)
                    }
                    4 -> {
                        val manager = ReviewManagerFactory.create(this)
                        val request = manager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // We got the ReviewInfo object
                                val reviewInfo = task.result
                                manager.launchReviewFlow(activity, reviewInfo)

                            }
                        }
                    }
                }
            }
        }
    }

    private fun getActiveFragment(): Fragment? {
        //Work-around to get the active fragment
        val navHostFragment: Fragment? = supportFragmentManager
            .findFragmentById(R.id.myNavHostFragment)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
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
        if (getActiveFragment() is WelcomeFragment || getActiveFragment() is ResolveMalformedToDosFragment)
            finish()
        else
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

    override fun onPause() {
        super.onPause()

        //Start service to apply any changes done on scheduling notifications
        startService(Intent(this, NextToDoService::class.java))
    }

    private fun popupSnackbar(
        activity: MainActivity,
        message: String,
        duration: Int,
        actionMessage: String,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            activity.coordinator_layout,
            message,
            duration
        ).apply {
            setAction(actionMessage, listener)
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.fabColor))
            setActionTextColor(activity.resources.getColor(android.R.color.black))
            show()
        }
    }
}