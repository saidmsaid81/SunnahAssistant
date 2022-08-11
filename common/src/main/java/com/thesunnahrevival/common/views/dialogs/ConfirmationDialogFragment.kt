package com.thesunnahrevival.common.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.thesunnahrevival.common.R

class ConfirmationDialogFragment: DialogFragment() {

    val titleKey = "title"
    val textKey = "text"
    val positiveButtonLabel = "positiveLabel"

    var mListener: DialogInterface.OnClickListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            return activity?.let {
                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(it)
                builder.setTitle(title)
                        .setMessage(text)
                        .setNegativeButton(getString(R.string.cancel)) {
                            _, _ -> dismiss()
                        }

                if (mListener != null){
                    builder.setPositiveButton(positiveLabel, mListener)
                }
                // Create the AlertDialog object and return it
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")

    }

    companion object {
        var title = ""
        var text = ""
        var positiveLabel = ""
    }

}

