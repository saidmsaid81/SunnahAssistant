package com.thesunnahrevival.sunnahassistant.views.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.translateLink
import kotlinx.android.synthetic.main.fragment_changelog.*

class ChangelogFragment : SunnahAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_changelog, container, false)


        //TODO update predefined reminders info
        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)
        mViewModel.settingsValue?.isAfterUpdate = false
        mViewModel.settingsValue?.categories?.add("Prayer")
        mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
        //viewModel.localeUpdate()


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