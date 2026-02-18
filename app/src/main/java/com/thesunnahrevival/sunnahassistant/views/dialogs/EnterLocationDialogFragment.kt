package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentAddLocationBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel


class EnterLocationDialogFragment : DialogFragment() {

    interface EnterLocationDialogListener {
        fun onLocationSaved()
        fun onLocationDialogCancelled()
    }

    private var mViewModel: SunnahAssistantViewModel? = null
    private var listener: EnterLocationDialogListener? = null

    private var _enterLocationDialogFragmentBinding: FragmentAddLocationBinding? = null
    private val enterLocationDialogFragmentBinding get() = _enterLocationDialogFragmentBinding!!

    fun setListener(listener: EnterLocationDialogListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)

        val builder = context?.let { AlertDialog.Builder(it) }
        val inflater = requireActivity().layoutInflater
        _enterLocationDialogFragmentBinding = FragmentAddLocationBinding.inflate(inflater)
        val view = enterLocationDialogFragmentBinding.root
        val settings = mViewModel?.settingsValue
        if (settings != null)
            enterLocationDialogFragmentBinding.location.setText(settings.formattedAddress)
        builder?.setView(view)
            ?.setPositiveButton(R.string.save, null)
            ?.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            ?.setTitle(getString(R.string.location_hint))
        val dialog = builder?.create()

        dialog?.setOnShowListener {
            handleDialogButtonClicks(dialog, view, mViewModel)
        }
        return dialog ?: super.onCreateDialog(savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onLocationDialogCancelled()
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
                viewModel.messages.observe(this) { message: String ->
                    if (message.matches("Successful".toRegex())) {
                        Toast.makeText(
                            requireContext(),
                            R.string.location_updated_successfully,
                            Toast.LENGTH_LONG
                        ).show()
                        listener?.onLocationSaved()
                        dialog.dismiss()
                    } else {
                        messagesTextView.text = message
                    }
                }
            }
            else {
                messagesTextView.text = getString(R.string.error_updating_location)
            }

        }
    }

}



