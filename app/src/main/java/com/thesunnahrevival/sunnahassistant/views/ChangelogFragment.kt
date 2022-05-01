package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.android.synthetic.main.changelog_layout.*

class ChangelogFragment: Fragment() {

    private lateinit var viewModel: SunnahAssistantViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.changelog_layout, container, false)
        val myActivity = activity

        if (myActivity != null){
            viewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.settingsValue?.isAfterUpdate = false
            viewModel.settingsValue?.categories?.add("Prayer")
            viewModel.settingsValue?.let { viewModel.updateSettings(it) }
            //viewModel.localeUpdate()

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        help_translate_app.setOnClickListener {
            translateLink(this)
        }
        send_feedback.setOnClickListener {
            startActivity(generateEmailIntent())
        }
        rate_this_app.setOnClickListener {
            openPlayStore(requireActivity(), requireActivity().packageName)
        }
    }

}