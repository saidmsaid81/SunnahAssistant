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
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.databinding.ReminderCardViewBinding
import com.thesunnahrevival.common.views.listeners.ReminderItemInteractionListener

class ReminderListAdapter(val context: Context) :
    PagingDataAdapter<Reminder, ReminderListAdapter.ViewHolder>(REMINDER_COMPARATOR) {
    private var mListener: ReminderItemInteractionListener? = null
    private lateinit var mLayoutInflater: LayoutInflater

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        mLayoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ReminderCardViewBinding>(
            mLayoutInflater,
            R.layout.reminder_card_view, viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentReminder = getItem(position)
        currentReminder?.let { viewHolder.bind(it) }
    }

    fun setOnItemInteractionListener(listener: ReminderItemInteractionListener?) {
        mListener = listener
    }

    fun deleteReminder(position: Int) {
        getItem(position)?.let { mListener?.onSwipeToDelete(position, it) }
    }

    fun markAsComplete(position: Int) {
        getItem(position)?.let { mListener?.onSwipeToMarkAsComplete(it) }
    }

    /**
     * Inner Class
     */
    inner class ViewHolder(private val binding: ReminderCardViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.reminder = reminder
            if (reminder.isComplete)
                binding.reminderTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            else
                binding.reminderTitle.paintFlags = 0
            binding.cardContent.setOnClickListener { view: View ->
                mListener?.launchReminderDetailsFragment(
                    view,
                    reminder
                )
            }
            binding.markAsComplete.setOnCheckedChangeListener { buttonView, isChecked ->
                mListener?.onMarkAsComplete(buttonView.isPressed, isChecked, reminder)
            }
        }
    }

    companion object {
        private val REMINDER_COMPARATOR = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
                return oldItem == newItem
            }
        }
    }
}