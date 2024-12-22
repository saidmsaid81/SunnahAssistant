package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Tip
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.databinding.FragmentTipsBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.views.adapters.TipsAdapter
import java.net.MalformedURLException

class TipsFragment : MenuBarFragment(), TipsAdapter.TipsItemInteractionListener {

    private lateinit var browser: InAppBrowser
    private var templateToDos: Map<Int, Pair<Int, ToDo>>? = null
    private var _tipsFragmentBinding: FragmentTipsBinding? = null
    private val tipsFragmentBinding get() = _tipsFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _tipsFragmentBinding = FragmentTipsBinding.inflate(inflater)
        browser = InAppBrowser(requireContext(), viewLifecycleOwner.lifecycleScope)
        return tipsFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TipsAdapter(this)
        val tips = arrayListOf<Tip>()

        templateToDos = mainActivityViewModel.getTemplateToDos()

        mainActivityViewModel.getTemplateToDoIds().observe(viewLifecycleOwner) { toDoIdsInDB: List<Int> ->
            val templates = templateToDos
            templates?.let {
                for (template in it.values) {
                    if (!toDoIdsInDB.contains(template.second.id)) {
                        tips.add(
                            Tip(
                                template.second.id,
                                template.second.name ?: "",
                                template.first,
                                template.second.predefinedToDoLink,
                                R.id.toDoDetailsFragment,
                                template.second.id
                            )
                        )
                    }
                }
            }
            val adapterData = getTips().apply {
                addAll(tips.distinct().shuffled())
            }
            adapter.setData(adapterData)
        }

        tipsFragmentBinding.recyclerView.adapter = adapter
    }

    private fun getTips(): ArrayList<Tip> {
        val tips = arrayListOf<Tip>()
        if (mainActivityViewModel.settingsValue?.isAutomaticPrayerAlertsEnabled == false ||
            mainActivityViewModel.settingsValue?.formattedAddress?.isBlank() == true
        )
            tips.add(
                Tip(
                    -900,
                    getString(R.string.prayer_time_alerts),
                    R.drawable.ic_mosque,
                    "",
                    R.id.prayerTimeSettingsFragment,
                    null
                )
            )
        return tips
    }

    override fun onSetupClickListener(launchFragment: Int, toDoId: Int?) {
        if (launchFragment == R.id.toDoDetailsFragment && toDoId != null) {
            templateToDos?.getOrDefault(toDoId, null)?.let {
                mainActivityViewModel.selectedToDo = it.second
                mainActivityViewModel.isToDoTemplate = true
            }
        }
        findNavController().navigate(launchFragment)
    }

    override fun onInfoClickListener(link: String) {
        try {
            browser.launchInAppBrowser(link, findNavController(), true)
        } catch (exception: MalformedURLException) {
            Log.e("MalformedURLException", exception.message.toString())
            Toast.makeText(requireContext(), getString(R.string.something_wrong), Toast.LENGTH_LONG)
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _tipsFragmentBinding = null
    }

}

