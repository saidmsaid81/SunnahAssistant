package com.thesunnahrevival.common.views.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.ToDo
import com.thesunnahrevival.common.databinding.ToDoCardViewBinding
import com.thesunnahrevival.common.views.listeners.ToDoItemInteractionListener

class ToDoListAdapter(val context: Context) :
    PagingDataAdapter<ToDo, ToDoListAdapter.ViewHolder>(ToDo_COMPARATOR) {
    private var mListener: ToDoItemInteractionListener? = null
    private lateinit var mLayoutInflater: LayoutInflater

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        mLayoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ToDoCardViewBinding>(
            mLayoutInflater,
            R.layout.to_do_card_view, viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentToDo = getItem(position)
        currentToDo?.let { viewHolder.bind(it) }
    }

    fun setOnItemInteractionListener(listener: ToDoItemInteractionListener?) {
        mListener = listener
    }

    fun deleteToDo(position: Int) {
        getItem(position)?.let { mListener?.onSwipeToDelete(position, it) }
    }

    fun markAsComplete(position: Int) {
        getItem(position)?.let { mListener?.onSwipeToMarkAsComplete(it) }
    }

    /**
     * Inner Class
     */
    inner class ViewHolder(private val binding: ToDoCardViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(toDo: ToDo) {
            binding.toDo = toDo
            if (toDo.isComplete)
                binding.toDoTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            else
                binding.toDoTitle.paintFlags = 0
            binding.cardContent.setOnClickListener { view: View ->
                mListener?.launchToDoDetailsFragment(
                    view,
                    toDo
                )
            }
            binding.markAsComplete.setOnCheckedChangeListener { buttonView, isChecked ->
                mListener?.onMarkAsComplete(buttonView.isPressed, isChecked, toDo)
            }
        }
    }

    companion object {
        private val ToDo_COMPARATOR = object : DiffUtil.ItemCallback<ToDo>() {
            override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
                return oldItem == newItem
            }
        }
    }
}