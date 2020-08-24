package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.createNotificationChannels
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

class ChangelogFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.changelog_layout, container, false)

        context?.let { createNotificationChannels(it) }
        val myActivity = activity

        if (myActivity != null){
            val viewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.settingsValue?.isAfterUpdate = false
            viewModel.settingsValue?.categories?.add("Prayer")
            viewModel.settingsValue?.let { viewModel.updateSettings(it) }
            viewModel.localeUpdate()

            view.findViewById<Button>(R.id.send_feedback).setOnClickListener {
                startActivity(generateEmailIntent())
            }
            view.findViewById<Button>(R.id.rate_this_app).setOnClickListener {
                openPlayStore(myActivity, myActivity.packageName)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

}