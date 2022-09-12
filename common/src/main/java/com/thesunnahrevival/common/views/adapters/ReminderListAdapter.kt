package com.thesunnahrevival.common.views.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.data.model.RemindersDiffCallback
import com.thesunnahrevival.common.databinding.ReminderCardViewBinding
import com.thesunnahrevival.common.views.listeners.ReminderItemInteractionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderListAdapter(val context: Context) :
    RecyclerView.Adapter<ReminderListAdapter.ViewHolder>() {
    private var mAllReminders: List<Reminder> = ArrayList()
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

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val currentReminder = mAllReminders[i]
        viewHolder.bind(currentReminder)
    }

    override fun getItemCount(): Int {
        return mAllReminders.size
    }

    fun setData(data: List<Reminder>) {
        CoroutineScope(Dispatchers.Default).launch {
            val diffResult = DiffUtil.calculateDiff(RemindersDiffCallback(mAllReminders, data))
            withContext(Dispatchers.Main) {
                mAllReminders = data
                diffResult.dispatchUpdatesTo(this@ReminderListAdapter)
            }
        }
    }

    /**
     * @param listener the listener that will handle when a RecyclerView item is clicked
     */
    fun setOnItemInteractionListener(listener: ReminderItemInteractionListener?) {
        mListener = listener
    }

    fun deleteReminder(position: Int) {
        mListener?.onSwipeToDelete(position)
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
            binding.cardView.setOnClickListener { view: View ->
                mListener?.launchReminderDetailsFragment(
                    view,
                    reminder
                )
            }
            binding.markAsComplete.setOnCheckedChangeListener { buttonView, isChecked ->
                mListener?.onMarkAsComplete(buttonView, isChecked, reminder)
            }
        }
    }
}