package com.thesunnahrevival.common.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.common.viewmodels.SunnahAssistantViewModel

open class SunnahAssistantFragment : Fragment() {
    lateinit var mViewModel: SunnahAssistantViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}