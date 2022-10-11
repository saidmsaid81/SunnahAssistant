package com.thesunnahrevival.sunnahassistant.views.toDoDetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.MalformedToDosAdapter
import kotlinx.android.synthetic.main.fragment_resolve_malformed_to_dos.*
import kotlinx.coroutines.flow.collect

class ResolveMalformedToDosFragment : SunnahAssistantFragment(),
    MalformedToDosAdapter.MalformedToDoInteractionListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_resolve_malformed_to_dos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MalformedToDosAdapter(this)
        recycler_view.adapter = adapter

        mViewModel.getSettings().observe(viewLifecycleOwner) {
            mViewModel.settingsValue = it
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mViewModel.getMalformedToDos().collect { toDos: List<ToDo> ->
                if (toDos.isNotEmpty()) {
                    adapter.setData(toDos)
                    delete_all.setOnClickListener {
                        showDeleteAllDialog(toDos)
                    }
                } else
                    findNavController().navigate(R.id.todayFragment)
            }
        }
    }

    private fun showDeleteAllDialog(toDos: List<ToDo>) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_all_malformed_todos))
            .setMessage(getString(R.string.confirm_deleting_malformed_to_dos))
            .setPositiveButton(R.string.yes) { _, _ ->
                mViewModel.deleteListOfToDos(toDos)
                Toast.makeText(requireContext(), R.string.delete_to_do, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    override fun onFixClickListener(toDo: ToDo) {
        mViewModel.isToDoTemplate = true
        mViewModel.selectedToDo = toDo
        findNavController().navigate(R.id.toDoDetailsFragment)
    }

    override fun onDeleteClickListener(toDo: ToDo) {
        view?.rootView?.let {
            mViewModel.deleteToDo(toDo)
            Snackbar.make(
                it, getString(R.string.delete_to_do),
                Snackbar.LENGTH_LONG
            ).apply {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
                setAction(getString(R.string.undo_delete)) { mViewModel.insertToDo(toDo) }
                setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                show()
            }
        }
    }
}