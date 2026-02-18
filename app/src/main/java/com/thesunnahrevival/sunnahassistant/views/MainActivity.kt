package com.thesunnahrevival.sunnahassistant.views

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
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
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.databinding.ActivityMainBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.REQUESTCODEFORUPDATE
import com.thesunnahrevival.sunnahassistant.utilities.REQUEST_NOTIFICATION_PERMISSION_CODE
import com.thesunnahrevival.sunnahassistant.utilities.SHARE
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORTED_LOCALES
import com.thesunnahrevival.sunnahassistant.utilities.PREDEFINED_TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.home.CalendarFragment
import com.thesunnahrevival.sunnahassistant.views.home.TodayFragment
import com.thesunnahrevival.sunnahassistant.views.others.WelcomeFragment
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.QuranReaderFragment
import com.thesunnahrevival.sunnahassistant.views.toDoDetails.ResolveMalformedToDosFragment
import com.thesunnahrevival.sunnahassistant.views.toDoDetails.ToDoDetailsFragment
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
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        mViewModel = ViewModelProvider(this)[SunnahAssistantViewModel::class.java]
        setSupportActionBar(findViewById(R.id.toolbar))

        navController = findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        activity = this
        firebaseAnalytics = Firebase.analytics
        getSettings()

        mViewModel.refreshScheduledReminders()

        handleIntents(intent)

        setupNavigation()
        handleEdgeToEdge()
        registerBackPressCallback()
    }

    private fun handleIntents(intent: Intent?) {
        val predefinedToDoId = if (intent?.extras?.containsKey(PREDEFINED_TO_DO_ID) == true) {
            intent.extras?.getInt(PREDEFINED_TO_DO_ID)
        } else {
            null
        }
        val link = intent?.extras?.getString("link")
        if (link != null) {
            launchInAppBrowser(link, predefinedToDoId)
        }

        if (intent?.action == SHARE) {
            showShareToDo()
        }

        if (link == null && predefinedToDoId != null) {
            openTemplateToDo(predefinedToDoId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntents(intent)
    }

    private fun setupNavigation() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination)
        }

        mainActivityBinding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.today -> navController.navigate(R.id.todayFragment)
                R.id.calendar -> navController.navigate(R.id.calendarFragment)
                R.id.tips -> navController.navigate(R.id.tipsFragment)
                R.id.resources -> navController.navigate(R.id.resourcesFragment)
            }
            true
        }
    }

    private fun onDestinationChanged(destination: NavDestination) {
        when (destination.id) {
            R.id.todayFragment,
            R.id.calendarFragment,
            R.id.tipsFragment,
            R.id.resourcesFragment -> {
                mainActivityBinding.bottomNavigationView.visibility = View.VISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            R.id.welcomeFragment,
            R.id.resolveMalformedToDosFragment,
            R.id.quranReaderFragment -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                mainActivityBinding.bottomNavigationView.visibility = View.GONE
            }

            else -> mainActivityBinding.bottomNavigationView.visibility = View.GONE
        }
    }
    private fun registerBackPressCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = getActiveFragment()
                when {
                    currentFragment is WelcomeFragment || currentFragment is ResolveMalformedToDosFragment -> {
                        finish()
                    }
                    else -> {
                        isEnabled = false
                        onSupportNavigateUp()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun handleEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(mainActivityBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            mViewModel.statusBarHeight.value = insets.top
            mViewModel.navBarHeight.value = insets.bottom

            val activeFragment = getActiveFragment()
            if (activeFragment !is QuranReaderFragment) {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                    topMargin = insets.top
                }
            } else {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = 0
                    bottomMargin = 0
                    rightMargin = 0
                    topMargin = 0
                }
            }

            WindowInsetsCompat.CONSUMED
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
                                }\n\n" +
                                getString(R.string.app_promotional_message, getSunnahAssistantAppLink(utmCampaign = "Share-To-Do"))
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

    private fun launchInAppBrowser(link: String, predefinedToDoId: Int? = null) {
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
                findNavController(R.id.myNavHostFragment),
                predefinedToDoId = predefinedToDoId
            )
    }

    private fun openTemplateToDo(toDoId: Int) {
        val templateToDos = mViewModel.getTemplateToDos()
        val template = templateToDos[toDoId] ?: return
        mViewModel.selectedToDo = template.second
        mViewModel.isToDoTemplate = true
        if (navController.currentDestination?.id != R.id.toDoDetailsFragment) {
            navController.navigate(R.id.toDoDetailsFragment)
        }
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
                    showAlarmPermissionDialogIfNeeded()
                }
            }
        }
    }

    private fun showAlarmPermissionDialogIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.enable_alarms_and_reminders_title))
                    .setMessage(getString(R.string.enable_alarms_and_reminders_message))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:$packageName".toUri()
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (getActiveFragment() is ToDoDetailsFragment) {
            mViewModel.isToDoTemplate = false
        }
        return if (!mainActivityBinding.bottomNavigationView.isVisible) {
            navController.navigateUp()
        } else {
            finish()
            true
        }
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
