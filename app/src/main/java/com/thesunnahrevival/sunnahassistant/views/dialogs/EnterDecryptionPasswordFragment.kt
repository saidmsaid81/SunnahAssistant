package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import kotlinx.android.synthetic.main.fragment_enter_decryption_password.*

class EnterDecryptionPasswordFragment : BottomSheetDialogFragment() {

    private var mListener: EnterDecryptionPasswordFragmentListener? = null
    private var mUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.fragment_enter_decryption_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mListener == null || mUri == null)
            dismiss()
        restore_data.setOnClickListener {
            val passwordInput = password.text.toString()
            if (passwordInput.isBlank())
                password.error = getString(R.string.password_cannot_be_blank)
            else {
                mListener?.onRestoreEncryptedDataClick(mUri, passwordInput)
                dismiss()
            }
        }
        cancel.setOnClickListener {
            mListener?.onRestoreEncryptedDataClick(null, "")
            dismiss()
        }
    }

    fun setListener(listener: EnterDecryptionPasswordFragmentListener) {
        mListener = listener
    }

    fun setDataUri(uri: Uri?) {
        mUri = uri
    }

    interface EnterDecryptionPasswordFragmentListener {
        fun onRestoreEncryptedDataClick(uri: Uri?, password: String)
    }
}