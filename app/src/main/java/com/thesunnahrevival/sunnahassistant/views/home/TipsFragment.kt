package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.Tip
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.databinding.FragmentTipsBinding
import com.thesunnahrevival.sunnahassistant.utilities.DONATION_APPEALS
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.TIP_ISLAMIC_RELIEF_GAZA_APPEAL_ID
import com.thesunnahrevival.sunnahassistant.utilities.TIP_PRAYER_TIME_ALERTS_ID
import com.thesunnahrevival.sunnahassistant.utilities.TIP_PRCS_PALESTINE_APPEAL_ID
import com.thesunnahrevival.sunnahassistant.utilities.TIP_UNRWA_GAZA_APPEAL_ID
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
        val tips = arrayListOf(getDonationAppeal())

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
                    TIP_PRAYER_TIME_ALERTS_ID,
                    getString(R.string.prayer_time_alerts),
                    R.drawable.ic_mosque,
                    "",
                    R.id.prayerTimeSettingsFragment,
                    null
                )
            )
        return tips
    }

    private fun getDonationAppeal(): Tip {
        val donationAppeals = listOf(
            Tip(
                TIP_UNRWA_GAZA_APPEAL_ID,
                "UNRWA Gaza Appeal",
                R.drawable.heart,
                "https://donate.unrwa.org/int/en/gaza",
                null,
                null
            ),
            Tip(
                TIP_PRCS_PALESTINE_APPEAL_ID,
                "PRCS Palestine Appeal",
                R.drawable.heart,
                "https://www.palestinercs.org/en",
                null,
                null
            ),
            Tip(
                TIP_ISLAMIC_RELIEF_GAZA_APPEAL_ID,
                "Islamic Relief Gaza Appeal",
                R.drawable.heart,
                "https://www.islamic-relief.org.uk/giving/appeals/palestine/gaza/",
                null,
                null
            )
        )
        return donationAppeals.random()
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

    override fun onInfoClickListener(link: String, toDoId: Int?, tipId: Int) {
        if (tipId in DONATION_APPEALS) {
            showDonationDisclosure(link)
            return
        }
        try {
            browser.launchInAppBrowser(
                link,
                findNavController(),
                true,
                toDoId
            )
        } catch (exception: MalformedURLException) {
            Log.e("MalformedURLException", exception.message.toString())
            Toast.makeText(requireContext(), getString(R.string.something_wrong), Toast.LENGTH_LONG)
                .show()
        }

    }

    private fun showDonationDisclosure(link: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.donation_disclosure_title))
            .setMessage(getString(R.string.donation_disclosure_message))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(getString(R.string.donation_disclosure_continue)) { _, _ ->
                launchDonationLink(link)
            }
            .show()
    }

    private fun launchDonationLink(link: String) {
        try {
            browser.launchInAppBrowser(
                link,
                findNavController(),
                false,
                null
            )
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
