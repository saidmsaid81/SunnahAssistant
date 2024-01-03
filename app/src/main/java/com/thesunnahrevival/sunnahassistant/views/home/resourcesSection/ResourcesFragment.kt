package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentResourcesBinding
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment

class ResourcesFragment : MenuBarFragment() {

    private var _resourcesFragmentBinding: FragmentResourcesBinding? = null

    private val resourcesFragmentBinding get() = _resourcesFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _resourcesFragmentBinding = FragmentResourcesBinding.inflate(inflater).apply {
            composeView.setContent {
                SunnahAssistantTheme {
                    val bottomNavViewHeight = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view).height.dp
                    ResourcesScreen(bottomNavViewHeight, findNavController())
                }

            }
        }
        return resourcesFragmentBinding.root
    }
}