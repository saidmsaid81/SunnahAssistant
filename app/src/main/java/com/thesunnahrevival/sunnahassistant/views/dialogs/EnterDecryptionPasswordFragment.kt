package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentEnterDecryptionPasswordBinding

class EnterDecryptionPasswordFragment : BottomSheetDialogFragment() {

    private var mListener: EnterDecryptionPasswordFragmentListener? = null
    private var mUri: Uri? = null
    private var _enterDecryptionPasswordFragmentBinding: FragmentEnterDecryptionPasswordBinding? =
        null
    private val enterDecryptionPasswordFragmentBinding get() = _enterDecryptionPasswordFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enterDecryptionPasswordFragmentBinding =
            FragmentEnterDecryptionPasswordBinding.inflate(inflater)
        isCancelable = false
        return enterDecryptionPasswordFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mListener == null || mUri == null)
            dismiss()
        enterDecryptionPasswordFragmentBinding.restoreData.setOnClickListener {
            val passwordInput = enterDecryptionPasswordFragmentBinding.password.text.toString()
            if (passwordInput.isBlank())
                enterDecryptionPasswordFragmentBinding.password.error =
                    getString(R.string.password_cannot_be_blank)
            else {
                mListener?.onRestoreEncryptedDataClick(mUri, passwordInput)
                dismiss()
            }
        }
        enterDecryptionPasswordFragmentBinding.cancel.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _enterDecryptionPasswordFragmentBinding = null
    }

    interface EnterDecryptionPasswordFragmentListener {
        fun onRestoreEncryptedDataClick(uri: Uri?, password: String)
    }
}