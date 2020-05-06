package com.thesunnahrevival.sunnahassistant.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel

class EnterLocationDialogFragment :DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = activity?.let { ViewModelProviders.of(it).get(SettingsViewModel::class.java) }

        val builder = context?.let { AlertDialog.Builder(it) }
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_location_layout, null)
        builder?.setView(view)
                ?.setPositiveButton(R.string.save, null)
                ?.setNegativeButton(R.string.cancel) {dialog, id -> dialog.cancel()}
                ?.setTitle(getString(R.string.location_hint))
        val dialog = builder?.create()

        dialog?.setOnShowListener {
            handleDialogButtonClicks(dialog, view, viewModel)
        }
        return dialog ?: super.onCreateDialog(savedInstanceState)
    }

    private fun handleDialogButtonClicks(dialog: AlertDialog, view: View, viewModel: SettingsViewModel?) {
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener() {
            val location = view.findViewById<EditText>(R.id.location).text.toString()
            val messagesTextView = view.findViewById<TextView>(R.id.location_fetch_messages)
            messagesTextView.visibility = View.VISIBLE
            if (location.isBlank() || location.isEmpty()) {
                messagesTextView.text = getString(R.string.location_cannot_be_empty)
                return@setOnClickListener
            }
            if (viewModel?.isDeviceOffline == false) {
                messagesTextView.text = getString(R.string.updating)
                viewModel.fetchGeocodingData(location)
                viewModel.observeGeocodingApiData()?.observe(this, Observer { data: GeocodingData? ->
                    if (data != null && data.results.isNotEmpty()) {
                        viewModel.updateLocationDetails(data)
                        dialog.dismiss()
                    } else if (data != null && data.results.isEmpty())
                        messagesTextView.text = getString(R.string.unavailable_location)
                    else
                        messagesTextView.text = getString(R.string.error_updating_location)
                })
            } else {
                messagesTextView.text = getString(R.string.error_updating_location)
            }

        }
    }
}
