package com.thesunnahrevival.common.views.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.viewmodels.SunnahAssistantViewModel

class AddCategoryDialogFragment : DialogFragment() {
    private lateinit var mViewModel: SunnahAssistantViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val myActivity = activity

        if (myActivity != null){

            val builder = AlertDialog.Builder(myActivity)
            mViewModel =
                ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)

            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.add_category_dialog_layout, null)

            builder.setView(view) // Add action buttons
                    .setPositiveButton(R.string.save) { _: DialogInterface?, _: Int ->
                        val settingsValue = mViewModel.settingsValue

                        if (settingsValue != null) {
                            val categoryEditText = view.findViewById<EditText>(R.id.category)
                            settingsValue.categories?.add(categoryEditText.text.toString())
                            mViewModel.updateSettings(settingsValue)
                            val inputManager =
                                    myActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                            inputManager.hideSoftInputFromWindow(view.applicationWindowToken,
                                    InputMethodManager.HIDE_NOT_ALWAYS)
                            category.value = categoryEditText.text.toString()
                        }
                    }
                    .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                    .setTitle(getString(R.string.add_new_category))
            return builder.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    companion object {
        var category = MutableLiveData<String>()
    }
}