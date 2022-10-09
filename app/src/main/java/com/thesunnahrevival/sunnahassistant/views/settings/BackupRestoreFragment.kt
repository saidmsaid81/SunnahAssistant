package com.thesunnahrevival.sunnahassistant.views.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.dialogs.EncryptBackupFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.EnterDecryptionPasswordFragment
import kotlinx.android.synthetic.main.fragment_backup_restore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


private const val WRITE_FILE = 0
private const val READ_FILE = 1

class BackupRestoreFragment : Fragment(), AdapterView.OnItemClickListener,
    EnterDecryptionPasswordFragment.EnterDecryptionPasswordFragmentListener,
    EncryptBackupFragment.EncryptBackupFragmentListener {

    private lateinit var mViewModel: SunnahAssistantViewModel
    private var password: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backup_restore, container, false)
        view.findViewById<TextView>(R.id.title).text = getString(R.string.backup_restore_data)
        val listView = view.findViewById<ListView>(R.id.options_list)
        listView.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.backup_restore_options)
            )
        listView.onItemClickListener = this

        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)
        return view
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            WRITE_FILE -> {
                val encryptBackupFragment = EncryptBackupFragment().apply {
                    setListener(this@BackupRestoreFragment)
                }
                encryptBackupFragment.show(requireActivity().supportFragmentManager, "backupData")
            }
            READ_FILE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "application/octet-stream"
                }
                startActivityForResult(intent, READ_FILE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            WRITE_FILE -> {
                updateStatusMessage(getString(R.string.backing_up_data_please_wait), null)
                val password = this.password
                val backupMessage = if (password != null)
                    mViewModel.backupEncryptedData(data?.data, password)
                else
                    mViewModel.backupPlainData(data?.data)
                updateStatusMessage(backupMessage.second, backupMessage.first)
                this.password = null
            }
            READ_FILE -> {
                mViewModel.viewModelScope.launch(Dispatchers.Main) {
                    updateStatusMessage(getString(R.string.restoring_data_please_wait), null)
                    val restoreMessage = mViewModel.restorePlainData(data?.data)
                    if (restoreMessage.first == null) {
                        val enterDecryptionPasswordFragment =
                            EnterDecryptionPasswordFragment().apply {
                                setListener(this@BackupRestoreFragment)
                                setDataUri(data?.data)
                            }
                        enterDecryptionPasswordFragment.show(
                            requireActivity().supportFragmentManager,
                            "enterDecryptionPassword"
                        )
                    } else
                        updateStatusMessage(restoreMessage.second, restoreMessage.first)
                }
            }
        }

    }

    private fun updateStatusMessage(statusMessage: String, status: Boolean?) {
        if (statusMessage.isBlank()) {
            message.visibility = View.GONE
            return
        }
        val textColor = if (status == null)
            android.R.color.holo_orange_dark
        else if (status)
            android.R.color.holo_green_light
        else
            android.R.color.holo_red_light
        message.text = statusMessage
        message.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        message.visibility = View.VISIBLE
    }

    override fun onRestoreEncryptedDataClick(uri: Uri?, password: String) {
        if (password.isBlank())
            updateStatusMessage("", null)
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            val encryptedRestoreMessage = mViewModel.restoreEncryptedData(uri, password)
            updateStatusMessage(
                encryptedRestoreMessage.second,
                encryptedRestoreMessage.first ?: false
            )
        }
    }

    override fun onBackupDataClick(password: String?) {
        if (password?.isBlank() == true) {
            updateStatusMessage("", null)
        } else {
            val timestamp = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH).format(Date())
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/x-sqlite3"
                putExtra(Intent.EXTRA_TITLE, "SunnahAssistant_$timestamp.db")
            }
            this.password = password
            startActivityForResult(intent, WRITE_FILE)
        }
    }
}