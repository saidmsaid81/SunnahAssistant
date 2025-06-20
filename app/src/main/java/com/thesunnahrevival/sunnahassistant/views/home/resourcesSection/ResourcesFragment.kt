package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
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
                SunnahAssistantTheme {
                    val surahs =
                        viewModel.getFirst5Surahs().collectAsState(initial = listOf())
                    ResourcesScreen(findNavController(), surahs)
                }
            }
        }
    }
}