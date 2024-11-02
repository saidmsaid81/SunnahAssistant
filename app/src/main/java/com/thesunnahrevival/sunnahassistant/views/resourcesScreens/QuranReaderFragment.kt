package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity

class QuranReaderFragment : SunnahAssistantFragment() {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)
        val pageNumbers = arguments?.getIntArray("pageNumbers")?.toList() ?: listOf()
        val quranPageAdapter = QuranPageAdapter(pageNumbers)
        quranReaderBinding.viewPager.adapter = quranPageAdapter
        quranReaderBinding.viewPager.reduceDragSensitivity(4)

        return quranReaderBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _quranReaderBinding = null
    }
}
