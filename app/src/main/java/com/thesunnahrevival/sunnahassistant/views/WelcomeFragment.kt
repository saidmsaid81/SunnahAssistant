package com.thesunnahrevival.sunnahassistant.views

import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.createNotificationChannels
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

class WelcomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let { createNotificationChannels(it) }
        val myActivity = activity

        if (myActivity != null){
            val viewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            viewModel.getSettings().observe(viewLifecycleOwner, Observer {
                if (it?.isFirstLaunch == true){
                    it.notificationToneUri = RingtoneManager.getActualDefaultRingtoneUri(
                            context, RingtoneManager.TYPE_NOTIFICATION)
                    it.isFirstLaunch = false
                    viewModel.updateSettings(it)
                }
                view.findViewById<Button>(R.id.quick_setup_button).setOnClickListener {
                    findNavController().navigate(R.id.mainFragment)
                }
            })
        }
    }
}