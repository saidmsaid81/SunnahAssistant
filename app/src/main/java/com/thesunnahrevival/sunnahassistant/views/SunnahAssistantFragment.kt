package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

open class SunnahAssistantFragment : Fragment() {
    protected lateinit var mViewModel: SunnahAssistantViewModel

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}