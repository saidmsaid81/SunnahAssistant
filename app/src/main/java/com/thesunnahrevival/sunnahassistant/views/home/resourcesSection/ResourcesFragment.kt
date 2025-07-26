package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.viewmodels.ResourcesViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment

class ResourcesFragment : MenuBarFragment() {

    private val viewModel: ResourcesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                val surahs by viewModel.getFirst5Surahs().collectAsState(initial = listOf())
                val isDataReady by mainActivityViewModel.prepopulateQuranDataCompletionStatus.collectAsState(initial = true)
                val resourceItems = viewModel.resourceItems()

                ResourcesScreen(
                    findNavController = findNavController(),
                    isDataReady = isDataReady,
                    surahs = surahs,
                    resourceItemList = resourceItems
                ) { surah ->
                    findNavController().navigate(R.id.quranReaderFragment)
                    mainActivityViewModel.updateCurrentPage(surah.startPage)
                }
            }
        }
    }
}
