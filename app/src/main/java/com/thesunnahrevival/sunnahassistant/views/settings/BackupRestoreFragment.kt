package com.thesunnahrevival.sunnahassistant.views.settings

import android.annotation.SuppressLint
import android.content.Intent
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
import com.thesunnahrevival.sunnahassistant.data.local.DB_NAME
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import kotlinx.android.synthetic.main.fragment_backup_restore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val WRITE_FILE = 1
private const val READ_FILE = 2

class BackupRestoreFragment : Fragment(), AdapterView.OnItemClickListener {
    private lateinit var mViewModel: SunnahAssistantViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backup_restore, container, false)
        view.findViewById<TextView>(R.id.title).text =
            "${getString(R.string.backup_restore_data)} ${getString(R.string.experimental_label)}"
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
            0 -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/x-sqlite3"
                    putExtra(Intent.EXTRA_TITLE, DB_NAME)
                }
                startActivityForResult(intent, WRITE_FILE)
            }
            1 -> {
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
                val backupMessage = mViewModel.backupData(data?.data)
                updateStatusMessage(backupMessage.second, backupMessage.first)
            }
            READ_FILE -> {
                mViewModel.viewModelScope.launch(Dispatchers.Main) {
                    updateStatusMessage(getString(R.string.restoring_data_please_wait), null)
                    val restoreMessage = mViewModel.restoreData(data?.data)
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
}