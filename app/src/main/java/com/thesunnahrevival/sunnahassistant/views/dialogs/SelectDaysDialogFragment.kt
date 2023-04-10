package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import java.text.DateFormatSymbols
import java.util.*


class SelectDaysDialogFragment : DialogFragment(), View.OnClickListener {

    private var listener: SelectDaysDialogListener? = null
    private var checkedDaysSet = TreeSet<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (listener == null)
            dismiss()

        initializeCheckedDays()

        val booleanArrayOfCheckedDays = BooleanArray(7) {
            false
        }

        checkedDaysSet.forEach {
            booleanArrayOfCheckedDays[it - 1] = true
        }

        val weekdays = DateFormatSymbols.getInstance(getLocale()).weekdays
            .filter { it.isNotBlank() }
            .toTypedArray()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_atleast_one_day)
            .setMultiChoiceItems(weekdays, booleanArrayOfCheckedDays) { _, which, isChecked ->
                if (isChecked)
                    checkedDaysSet.add(which + 1)
                else if (checkedDaysSet.contains(which + 1))
                    checkedDaysSet.remove(which + 1)
            }
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton =
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener(this)
        }

        return dialog
    }

    private fun initializeCheckedDays() {
        val daysArgument = arguments?.getSerializable(DAYS)

        val checkedDaysList = if (daysArgument is TreeSet<*>)
            daysArgument.filterIsInstance<Int>()
        else
            listOf()

        checkedDaysSet = TreeSet<Int>().apply {
            addAll(checkedDaysList)
        }
    }

    override fun onClick(v: View?) {
        if (checkedDaysSet.isEmpty()) {
            Toast.makeText(requireContext(), R.string.select_atleast_one_day, Toast.LENGTH_SHORT)
                .show()
            return
        }
        listener?.onSelectDaysDialogPositiveClick(checkedDaysSet)
        dismiss()
    }

    fun setListener(listener: SelectDaysDialogListener) {
        this.listener = listener
    }

    interface SelectDaysDialogListener {
        fun onSelectDaysDialogPositiveClick(checkedDays: TreeSet<Int>)
    }

    companion object {
        const val DAYS = "days"
    }

}