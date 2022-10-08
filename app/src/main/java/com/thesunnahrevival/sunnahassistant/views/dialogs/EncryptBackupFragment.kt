package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import kotlinx.android.synthetic.main.fragment_encrypt_backup.*

class EncryptBackupFragment : BottomSheetDialogFragment() {
    private var mListener: EncryptBackupFragmentListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_encrypt_backup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mListener == null)
            dismiss()

        toggle_encryption.setOnCheckedChangeListener { _, isChecked ->
            enter_password_layout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        backup_data.setOnClickListener {
            if (toggle_encryption.isChecked) {
                if (validatePassword()) {
                    mListener?.onBackupDataClick(password.text.toString())
                    dismiss()
                }
            } else {
                mListener?.onBackupDataClick(null)
                dismiss()
            }
        }

        cancel.setOnClickListener {
            mListener?.onBackupDataClick("")
            dismiss()
        }
    }

    private fun validatePassword(): Boolean {
        val passwordInput = password.text.toString()
        val confirmPasswordInput = confirm_password.text.toString()
        if (passwordInput.isBlank()) {
            password.error = getString(R.string.password_cannot_be_blank)
            return false
        }

        if (confirmPasswordInput.isBlank()) {
            confirm_password.error = getString(R.string.password_cannot_be_blank)
            return false
        }

        if (passwordInput != confirmPasswordInput) {
            confirm_password.error = getString(R.string.password_doesnt_match)
            return false
        }
        return true
    }

    fun setListener(listener: EncryptBackupFragmentListener) {
        mListener = listener
    }

    interface EncryptBackupFragmentListener {
        fun onBackupDataClick(password: String?)
    }
}