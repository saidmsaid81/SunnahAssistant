package com.thesunnahrevival.sunnahassistant.views

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.play.core.review.ReviewManagerFactory
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this,navController)

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
                println(numberOfLaunches)
                //Set theme
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) //Android 9 and below
                    if (settings?.isLightMode != false)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //Dark Mode

                if ((settings?.numberOfLaunches ?: 0) > 0 && (settings?.numberOfLaunches ?: 0) % 7 == 0){
                        showInAppReviewPrompt()
                }
            }
        }
    }

    private fun showInAppReviewPrompt() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            if (it.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = it.result
                manager.launchReviewFlow(this, reviewInfo)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }
}