package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentEncryptBackupBinding

class EncryptBackupFragment : BottomSheetDialogFragment() {
    private var mListener: EncryptBackupFragmentListener? = null
    private var _encryptBackupFragment: FragmentEncryptBackupBinding? = null
    private val encryptBackupFragment get() = _encryptBackupFragment!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _encryptBackupFragment = FragmentEncryptBackupBinding.inflate(inflater)
        isCancelable = false
        return encryptBackupFragment.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mListener == null)
            dismiss()

        encryptBackupFragment.toggleEncryption.setOnCheckedChangeListener { _, isChecked ->
            encryptBackupFragment.enterPasswordLayout.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        encryptBackupFragment.backupData.setOnClickListener {
            if (encryptBackupFragment.toggleEncryption.isChecked) {
                if (validatePassword()) {
                    mListener?.onBackupDataClick(encryptBackupFragment.password.text.toString())
                    dismiss()
                }
            } else {
                mListener?.onBackupDataClick(null)
                dismiss()
            }
        }

        encryptBackupFragment.cancel.setOnClickListener {
            mListener?.onBackupDataClick("")
            dismiss()
        }
    }

    private fun validatePassword(): Boolean {
        val passwordInput = encryptBackupFragment.password.text.toString()
        val confirmPasswordInput = encryptBackupFragment.confirmPassword.text.toString()
        if (passwordInput.isBlank()) {
            encryptBackupFragment.password.error = getString(R.string.password_cannot_be_blank)
            return false
        }

        if (confirmPasswordInput.isBlank()) {
            encryptBackupFragment.confirmPassword.error =
                getString(R.string.password_cannot_be_blank)
            return false
        }

        if (passwordInput != confirmPasswordInput) {
            encryptBackupFragment.confirmPassword.error = getString(R.string.password_doesnt_match)
            return false
        }
        return true
    }

    fun setListener(listener: EncryptBackupFragmentListener) {
        mListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _encryptBackupFragment = null
    }

    interface EncryptBackupFragmentListener {
        fun onBackupDataClick(password: String?)
    }
}