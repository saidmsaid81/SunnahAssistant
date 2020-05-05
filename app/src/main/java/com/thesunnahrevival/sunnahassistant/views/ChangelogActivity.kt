package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil

class ChangelogActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.changelog_layout)
        findViewById<Button>(R.id.send_feedback).setOnClickListener { startActivity(SunnahAssistantUtil.generateEmailIntent()) }
        findViewById<Button>(R.id.rate_this_app).setOnClickListener { SunnahAssistantUtil.openPlayStore(this, packageName) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}