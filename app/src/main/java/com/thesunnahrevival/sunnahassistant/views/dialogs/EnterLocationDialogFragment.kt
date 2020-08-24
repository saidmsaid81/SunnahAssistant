package com.thesunnahrevival.sunnahassistant.views.dialogs

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
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.android.synthetic.main.add_location_layout.view.*


class EnterLocationDialogFragment :DialogFragment() {

    private var mViewModel: SunnahAssistantViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mViewModel = activity?.let { ViewModelProviders.of(it).get(SunnahAssistantViewModel::class.java) }

        val builder = context?.let { AlertDialog.Builder(it) }
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_location_layout, null)
        val settings = mViewModel?.settingsValue
        if (settings != null)
            view.location.setText(settings.formattedAddress)
        builder?.setView(view)
                ?.setPositiveButton(R.string.save, null)
                ?.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel()}
                ?.setTitle(getString(R.string.location_hint))
        val dialog = builder?.create()

        dialog?.setOnShowListener {
            handleDialogButtonClicks(dialog, view, mViewModel)
        }
        return dialog ?: super.onCreateDialog(savedInstanceState)
    }

    private fun handleDialogButtonClicks(dialog: AlertDialog, view: View, viewModel: SunnahAssistantViewModel?) {
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val location = view.findViewById<EditText>(R.id.location).text.toString()
            val messagesTextView = view.findViewById<TextView>(R.id.location_fetch_messages)
            messagesTextView.visibility = View.VISIBLE
            if (location.isBlank() || location.isEmpty()) {
                messagesTextView.text = getString(R.string.location_cannot_be_empty)
                return@setOnClickListener
            }
            if (viewModel?.isDeviceOffline == false) {
                messagesTextView.text = getString(R.string.updating)
                viewModel.getGeocodingData(location)
                viewModel.messages.observe(this, Observer { message: String ->
                    if (message.matches("Successful".toRegex()))
                        dialog.dismiss()
                    else
                        messagesTextView.text = message
                })
            }
            else {
                messagesTextView.text = getString(R.string.error_updating_location)
            }

        }
    }

}



