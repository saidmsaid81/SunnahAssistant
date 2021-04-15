package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class SettingsListFragment : Fragment(), AdapterView.OnItemClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_lists, container, false)
        val listView = view.findViewById<ListView>(R.id.settings_lists)
        listView.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.settings_lists))
        listView.onItemClickListener = this
        view.findViewById<TextView>(R.id.version).text = String.format(getString(R.string.version), BuildConfig.VERSION_NAME)
        return view
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> findNavController().navigate(R.id.hijriDateSettingsFragment)
            1 -> findNavController().navigate(R.id.prayerTimeSettingsFragment)
            2 -> findNavController().navigate(R.id.customizeCategoriesFragment)
            3 -> findNavController().navigate(R.id.notificationSettingsFragment)
            4 -> findNavController().navigate(R.id.layoutSettingsFragment)
            5 -> findNavController().navigate(R.id.privacySettingsFragment)
            6 -> {
                val intent = generateEmailIntent()
                if (intent.resolveActivity(requireActivity().packageManager) != null)
                    startActivity(intent)
                else
                    Toast.makeText(context, getString(R.string.no_email_app_installed), Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}