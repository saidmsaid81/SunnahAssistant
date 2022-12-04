package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.services.NextToDoService
import com.thesunnahrevival.sunnahassistant.utilities.createNotificationChannels
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


open class MainActivity : AppCompatActivity() {

    private lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannels(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        activity = this
        getSettings()

        startService(Intent(this, NextToDoService::class.java))

        if (intent.extras?.get("link") != null)
            findNavController(R.id.myNavHostFragment).navigate(R.id.webviewFragment, intent.extras)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todayFragment -> bottom_navigation_view.visibility = View.VISIBLE
                R.id.calendarFragment, R.id.tipsFragment -> {
                    bottom_navigation_view.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                R.id.welcomeFragment, R.id.resolveMalformedToDosFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottom_navigation_view.visibility = View.GONE
                }
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

        else if (settings.numberOfLaunches > 0 && settings.numberOfLaunches % 5 == 0) {
            val random = Random.nextInt(1, 5)
            val fragment = getActiveFragment()

            if (fragment is TodayFragment && fragment !is CalendarFragment) {
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

    override fun onPause() {
        super.onPause()

        //Start service to apply any changes done on scheduling notifications
        startService(Intent(this, NextToDoService::class.java))
    }
}