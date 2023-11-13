package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.TO_DO_ID
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel

class SnoozeOptionsFragment : DialogFragment(), AdapterView.OnItemClickListener {

    private lateinit var mViewModel: SunnahAssistantViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mViewModel = ViewModelProvider(requireActivity())[SunnahAssistantViewModel::class.java]
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_snooze_options, null)
        val listView = view.findViewById<ListView>(R.id.options)
        listView.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.snooze_options)
            )
        listView.onItemClickListener = this
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle(getString(R.string.snooze_for))
            .create()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val toDoId = arguments?.getInt(TO_DO_ID) ?: 0
        val minutes = when (position) {
            0 -> 5
            1 -> 15
            2 -> 30
            3 -> 45
            4 -> 60
            else -> 0
        }

        mViewModel.snoozeNotification(toDoId, minutes)
        dismiss()
    }
}