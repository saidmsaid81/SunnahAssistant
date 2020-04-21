package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.thesunnahrevival.sunnahassistant.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val transaction = supportFragmentManager.beginTransaction()
        val fragmentToShow = intent.getStringExtra("fragmentToShow") ?: "default"
        val fragment = if (fragmentToShow.matches("prayer".toRegex()))
            SettingsFragment.newInstance(R.layout.prayer_time_settings)
        else
            SettingsFragment.newInstance(R.layout.settings_lists)
        transaction.replace(R.id.settings_container, fragment )
        transaction.commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> super.onBackPressed()
        }
        return true
    }

}