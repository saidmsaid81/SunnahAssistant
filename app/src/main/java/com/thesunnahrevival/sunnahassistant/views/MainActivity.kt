package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
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
import com.thesunnahrevival.sunnahassistant.databinding.ActivityMainBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.REQUESTCODEFORUPDATE
import com.thesunnahrevival.sunnahassistant.utilities.REQUEST_NOTIFICATION_PERMISSION_CODE
import com.thesunnahrevival.sunnahassistant.utilities.SHARE
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORTED_LOCALES
import com.thesunnahrevival.sunnahassistant.utilities.TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.createNotificationChannels
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.home.CalendarFragment
import com.thesunnahrevival.sunnahassistant.views.home.TodayFragment
import com.thesunnahrevival.sunnahassistant.views.others.WelcomeFragment
import com.thesunnahrevival.sunnahassistant.views.toDoDetails.ResolveMalformedToDosFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random


open class MainActivity : AppCompatActivity() {

    private lateinit var activity: MainActivity
    lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mViewModel: SunnahAssistantViewModel
    private lateinit var mainActivityBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        mViewModel = ViewModelProvider(this)[SunnahAssistantViewModel::class.java]
        createNotificationChannels(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        activity = this
        firebaseAnalytics = Firebase.analytics
        getSettings()

        mViewModel.refreshScheduledReminders()

        val link = intent.extras?.getString("link")
        if (link != null) {
            launchInAppBrowser(link)
        }

        if (intent.action == SHARE) {
            showShareToDo()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todayFragment -> mainActivityBinding.bottomNavigationView.visibility =
                    View.VISIBLE

                R.id.calendarFragment, R.id.tipsFragment -> {
                    mainActivityBinding.bottomNavigationView.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                R.id.welcomeFragment, R.id.resolveMalformedToDosFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    mainActivityBinding.bottomNavigationView.visibility = View.GONE
                }

                R.id.changelogFragment, R.id.toDoDetailsFragment -> {
                    mainActivityBinding.bottomNavigationView.visibility = View.GONE
                }

                else -> mainActivityBinding.bottomNavigationView.visibility = View.GONE
            }
        }

        mainActivityBinding.bottomNavigationView.setOnNavigationItemSelectedListener {
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

    private fun showShareToDo() {
        val toDoId = intent.extras?.getInt(TO_DO_ID) ?: 0
        Toast.makeText(
            this,
            getString(R.string.launching_share_menu_please_wait),
            Toast.LENGTH_SHORT
        ).show()

        lifecycleScope.launch(Dispatchers.IO) {
            val toDo = mViewModel.getToDoById(toDoId)
            if (toDo != null) {
                withContext(Dispatchers.Main) {
                    val now = LocalDate.now()
                    val date = "${now.dayOfMonth} ${
                        now.month.name.lowercase().replaceFirstChar { it.titlecase() }
                    }, ${now.year}"

                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(R.string.to_do)}: ${toDo.name}\n" +
                                "${getString(R.string.to_do_category)}: ${toDo.category}\n" +
                                "${getString(R.string.date)}: $date\n" +
                                "${getString(R.string.time_label)}: ${
                                    formatTimeInMilliseconds(
                                        applicationContext,
                                        toDo.timeInMilliseconds
                                    )
                                }\n" +
                                "${getString(R.string.powered_by_sunnah_assistant)}\n\n" +
                                "Get Sunnah Assistant App at\n" +
                                "https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
                    )
                    val chooserIntent = Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_to_do)
                    )
                    startActivity(chooserIntent)
                }
            }
        }
    }

    private fun launchInAppBrowser(link: String) {
        if (link.contains("market://details")) {
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

    private fun getSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = mViewModel.getAppSettingsValue()
            var numberOfLaunches = settings?.numberOfLaunches
            if (numberOfLaunches != null) {
                numberOfLaunches++
                settings?.numberOfLaunches = numberOfLaunches
                settings?.let { mViewModel.updateSettings(it) }

            }

            withContext(Dispatchers.Main) {
                if (settings != null) {
                    if (!settings.language.matches(Locale.getDefault().language.toRegex())) {
                        mViewModel.localeUpdate()
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
                        REQUESTCODEFORUPDATE
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
                        if (!SUPPORTED_LOCALES.contains(Locale.getDefault().language))
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val activeFragment = getActiveFragment()
                    if (activeFragment is TodayFragment) {
                        activeFragment.mBinding.banner.dismiss()
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

    @Deprecated("Deprecated in Java")
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
        mViewModel.refreshScheduledReminders()
    }

    private fun popupSnackbar(
        activity: MainActivity,
        message: String,
        duration: Int,
        actionMessage: String,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            mainActivityBinding.coordinatorLayout,
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