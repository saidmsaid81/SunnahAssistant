package com.thesunnahrevival.sunnahassistant.views.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent

class SettingsListFragment : Fragment(), AdapterView.OnItemClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_lists, container, false)
        val listView = view.findViewById<ListView>(R.id.settings_lists)
        listView.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.settings_lists)
            )
        listView.onItemClickListener = this
        view.findViewById<TextView>(R.id.version).text =
            String.format(getString(R.string.version), BuildConfig.VERSION_NAME)

        view.findViewById<ImageView>(R.id.website).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.thesunnahrevival.com")
                )
            )
        }
        view.findViewById<ImageView>(R.id.facebook).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/thesunnahrevival")
                )
            )
        }
        view.findViewById<ImageView>(R.id.twitter).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.twitter.com/thesunahrevival")
                )
            )
        }
        view.findViewById<ImageView>(R.id.instagram).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/thesunnahrevival")
                )
            )
        }
        view.findViewById<ImageView>(R.id.telegram).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/thesunnahrevival")
                )
            )
        }
        return view
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> findNavController().navigate(R.id.hijriDateSettingsFragment)
            1 -> findNavController().navigate(R.id.prayerTimeSettingsFragment)
            2 -> findNavController().navigate(R.id.customizeCategoriesFragment)
            3 -> findNavController().navigate(R.id.notificationSettingsFragment)
            4 -> findNavController().navigate(R.id.layoutSettingsFragment)
            5 -> findNavController().navigate(R.id.privacyPolicyFragment)
            6 -> {
                val intent = generateEmailIntent()
                if (intent.resolveActivity(requireActivity().packageManager) != null)
                    startActivity(intent)
                else
                    Toast.makeText(
                        context,
                        getString(R.string.no_email_app_installed),
                        Toast.LENGTH_LONG
                    ).show()
            }
        }

    }
}