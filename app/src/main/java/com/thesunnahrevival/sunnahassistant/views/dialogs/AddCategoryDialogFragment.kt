package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

class AddCategoryDialogFragment : DialogFragment() {
    private lateinit var mViewModel: SunnahAssistantViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        mViewModel =
            ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_category_dialog_layout, null)

        builder.setView(view) // Add action buttons
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setTitle(getString(R.string.add_new_category))

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val settingsValue = mViewModel.settingsValue

                if (settingsValue != null) {
                    val categoryEditText = view.findViewById<EditText>(R.id.category)
                    if (categoryEditText.text.isNotBlank()) {
                        settingsValue.categories?.add(categoryEditText.text.toString())
                        mViewModel.updateSettings(settingsValue)
                        category.value = categoryEditText.text.toString()
                        dismiss()
                    } else {
                        view.findViewById<TextView>(R.id.category_error).visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                }
            }
        }
        return dialog
    }

    companion object {
        var category = MutableLiveData<String>()
    }
}