package com.thesunnahrevival.common.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.utilities.getLocale
import java.text.DateFormatSymbols
import java.util.*


class SelectDaysDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.select_days_dialog_fragment, null)
        val selectDaysLayout = view.findViewById<LinearLayout>(R.id.select_days_layout)
        val weekdays = DateFormatSymbols.getInstance(getLocale()).weekdays
        val checkedDays = selectedDays.value

        for ((index, week) in weekdays.withIndex()) {
            if (week.isNotBlank()) {
                val checkbox = CheckBox(requireContext())
                checkbox.text = week
                checkbox.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkbox.setTextAppearance(
                        android.R.style.TextAppearance_Material_Widget_TextView_PopupMenu
                    )
                } else
                    checkbox.textSize = 14F

                checkbox.isChecked = selectedDays.value?.contains(index) == true
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        checkedDays?.add(index)
                    else
                        checkedDays?.remove(index)
                }

                selectDaysLayout.addView(checkbox)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                selectedDays.value = checkedDays
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }

    companion object {
        val selectedDays = MutableLiveData(TreeSet<Int>())
    }
}