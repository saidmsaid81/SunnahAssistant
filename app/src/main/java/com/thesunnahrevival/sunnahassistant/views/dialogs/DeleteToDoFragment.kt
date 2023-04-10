package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.formatDate
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.listeners.ToDoItemInteractionListener
import java.util.*

class DeleteToDoFragment : DialogFragment() {
    private var toDoItemInteractionListener: ToDoItemInteractionListener? = null
    private var onDismissListener: OnDismissListener? = null
    private lateinit var mViewModel: SunnahAssistantViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mViewModel = ViewModelProvider(requireActivity())[SunnahAssistantViewModel::class.java]
        val toDo = arguments?.getSerializable(TODO) as ToDo?

        toDo?.let {
            if (it.isAutomaticPrayerTime()) {
                toDoItemInteractionListener?.showPrayerTimeDeletionError()
                dismiss()
            }

            if (it.frequency == Frequency.OneTime) {
                mViewModel.deleteToDo(it)
                onDismissListener?.onDismiss()
                toDoItemInteractionListener?.showUndoDeleteSnackbar(it)
                dismiss()
            }
        }
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_delete_repetitive_to_do, null)
        view.findViewById<TextView>(R.id.delete_only_this_date).text =
            getString(R.string.delete_only_this_date, mViewModel.selectedToDoDate.formatDate())
        val futureRepetitonsView = view.findViewById<TextView>(R.id.delete_future_repetitions)
        futureRepetitonsView.text =
            getString(R.string.delete_future_repetitions, mViewModel.selectedToDoDate.formatDate())

        builder.setView(view)
            ?.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            ?.setTitle(getString(R.string.delete_repetitive_to_do))

        if (toDo?.endsOnDate == mViewModel.selectedToDoDate.toString())
            futureRepetitonsView.visibility = View.GONE
        else
            futureRepetitonsView.visibility = View.VISIBLE

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            toDo?.let {

                when (checkedId) {
                    R.id.delete_only_this_date -> {
                        val deletedDates = TreeSet<String>()
                        deletedDates.addAll(it.deletedDates)
                        deletedDates.add(mViewModel.selectedToDoDate.toString())
                        val newToDo = it.copy(deletedDates = deletedDates)
                        mViewModel.insertToDo(newToDo)
                    }
                    R.id.delete_future_repetitions -> {
                        val newToDo = it.copy(endsOnDate = mViewModel.selectedToDoDate.toString())
                        mViewModel.insertToDo(newToDo)
                    }
                    R.id.delete_all -> mViewModel.deleteToDo(it)
                }

                onDismissListener?.onDismiss()
                toDoItemInteractionListener?.showUndoDeleteSnackbar(it)
                dismiss()
            }
        }

        return builder.create()
    }

    fun setOnToDoDeleteListener(toDoItemInteractionListener: ToDoItemInteractionListener) {
        this.toDoItemInteractionListener = toDoItemInteractionListener
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    companion object {
        const val TODO: String = "TODO_ID"
    }
}