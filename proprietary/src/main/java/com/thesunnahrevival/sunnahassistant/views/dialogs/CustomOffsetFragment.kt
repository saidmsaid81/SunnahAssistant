package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.thesunnahrevival.sunnahassistant.R
import java.lang.NumberFormatException

class CustomOffsetFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_offset_fragment, null)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.offset_options, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val offsetSpinner = view.findViewById<Spinner>(R.id.offset_spinner)
        offsetSpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(com.thesunnahrevival.common.R.string.save) { _: DialogInterface?, _: Int ->
                onPositiveButtonClick(view)
            }
            .setNegativeButton(com.thesunnahrevival.common.R.string.cancel)
            { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setTitle(getString(R.string.custom_reminder_offset))
        return dialog.create()
    }

    private fun onPositiveButtonClick(view: View) {
        try {
            val hoursInput = view.findViewById<EditText>(R.id.hours_value)?.text.toString()
            val hours = if (hoursInput.isNotBlank()) hoursInput.toInt() else 0
            val minutesInput = view.findViewById<EditText>(R.id.minutes_value)?.text.toString()
            val minutes = if (minutesInput.isNotBlank()) minutesInput.toInt() else 0
            val isBefore = view.findViewById<Spinner>(R.id.offset_spinner).selectedItemPosition == 0
            val offsetInMinutes = (hours * 60) + minutes
            customOffsetInMinutes.value = if (isBefore) -offsetInMinutes else offsetInMinutes
        } catch (exception: NumberFormatException) {
            customOffsetInMinutes.value = 0
        }
    }

    companion object {
        val customOffsetInMinutes = MutableLiveData<Int?>()
    }
}