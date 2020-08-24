package com.thesunnahrevival.sunnahassistant.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.databinding.AltReminderCardViewBinding
import com.thesunnahrevival.sunnahassistant.databinding.ReminderCardViewBinding
import com.thesunnahrevival.sunnahassistant.views.interfaces.OnDeleteReminderListener
import com.thesunnahrevival.sunnahassistant.views.interfaces.ReminderItemInteractionListener
import java.util.*

class ReminderListAdapter(val context: Context, private val mIsExpandedLayout: Boolean) : RecyclerView.Adapter<ReminderListAdapter.ViewHolder>() {
    private var mAllReminders: List<Reminder> = ArrayList()
    private var mListener: ReminderItemInteractionListener? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mDeleteReminderListener: OnDeleteReminderListener? = null
    private var mFrequency = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        mLayoutInflater = LayoutInflater.from(context)
        val layoutId: Int = if (mIsExpandedLayout)
            R.layout.reminder_card_view
        else
            R.layout.alt_reminder_card_view
        val binding = DataBindingUtil.inflate<ViewDataBinding>(mLayoutInflater,
                layoutId, viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val currentReminder = mAllReminders[i]
        viewHolder.bind(currentReminder)
    }

    override fun getItemCount(): Int {
        return mAllReminders.size
    }

    fun setData(data: List<Reminder>, frequency: Int) {
        mAllReminders = data
        mFrequency = frequency
        notifyDataSetChanged()
    }

    /**
     * @param listener the listener that will handle when a RecyclerView item is clicked
     */
    fun setOnItemClickListener(listener: ReminderItemInteractionListener?) {
        mListener = listener
    }

    fun setDeleteReminderListener(deleteReminderListener: OnDeleteReminderListener) {
        mDeleteReminderListener = deleteReminderListener
    }

    fun deleteReminder(position: Int) {
        mDeleteReminderListener?.deleteReminder(position)
    }

    /**
     * Inner Class
     */
    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.setVariable(BR.reminder, reminder)
            if (binding is ReminderCardViewBinding) {
                binding.toggleButton.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean -> mListener?.onToggleButtonClick(buttonView, isChecked, reminder) }
                binding.cardView.setOnClickListener { view: View -> mListener?.openBottomSheet(view, reminder) }
                binding.frequency = mFrequency
            } else if (binding is AltReminderCardViewBinding) {
                binding.toggleButton.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean -> mListener?.onToggleButtonClick(buttonView, isChecked, reminder) }
                binding.cardView.setOnClickListener { view: View -> mListener?.openBottomSheet(view, reminder) }
            }
        }

    }

}