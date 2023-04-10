package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.thesunnahrevival.sunnahassistant.R

class EnterOffsetFragment : DialogFragment() {
    private var index: Int? = null
    private var listener: EnterOffsetFragmentListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (listener == null)
            dismiss()

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_enter_offset, null)

        val currentValue = arguments?.getInt(CURRENT_VALUE) ?: 0
        val hours = currentValue / 60
        val minutes = currentValue % 60
        val selection = if (currentValue < 1) 0 else 1

        view.findViewById<EditText>(R.id.hours_value).text =
            Editable.Factory.getInstance().newEditable(hours.toUnsignedInt().toString())
        view.findViewById<EditText>(R.id.minutes_value).text =
            Editable.Factory.getInstance().newEditable(minutes.toUnsignedInt().toString())

        val adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.offset_options, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val offsetSpinner = view.findViewById<Spinner>(R.id.offset_spinner)
        offsetSpinner.adapter = adapter
        offsetSpinner.setSelection(selection)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.save) { _: DialogInterface?, _: Int ->
                onPositiveButtonClick(view)
            }
            .setNegativeButton(R.string.cancel)
            { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setTitle(getString(R.string.custom_reminder_offset))
        return dialog.create()
    }

    fun setListener(listener: EnterOffsetFragmentListener, index: Int) {
        this.listener = listener
        this.index = index
    }

    private fun onPositiveButtonClick(view: View) {
        try {
            val hoursInput = view.findViewById<EditText>(R.id.hours_value)?.text.toString()
            val hours = if (hoursInput.isNotBlank()) hoursInput.toInt() else 0
            val minutesInput = view.findViewById<EditText>(R.id.minutes_value)?.text.toString()
            val minutes = if (minutesInput.isNotBlank()) minutesInput.toInt() else 0
            val isBefore = view.findViewById<Spinner>(R.id.offset_spinner).selectedItemPosition == 0
            val offsetInMinutes = (hours * 60) + minutes
            listener?.onOffsetSave(if (isBefore) -offsetInMinutes else offsetInMinutes, index ?: -1)
        } catch (exception: NumberFormatException) {
            listener?.onOffsetSave(0, index ?: -1)
        }
    }

    interface EnterOffsetFragmentListener {
        fun onOffsetSave(offsetInMinutes: Int, index: Int)
    }

    companion object {
        const val CURRENT_VALUE = "currentValue"
    }

}

private fun Int.toUnsignedInt(): Int {
    if (this < 0)
        return this - (this * 2)
    return this
}
