package com.thesunnahrevival.common.views.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.utilities.generateEmailIntent
import com.thesunnahrevival.common.utilities.openPlayStore
import com.thesunnahrevival.common.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.common.views.SunnahAssistantFragment
import com.thesunnahrevival.common.views.translateLink
import kotlinx.android.synthetic.main.changelog_layout.*

class ChangelogFragment : SunnahAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.changelog_layout, container, false)


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