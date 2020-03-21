package com.thesunnahrevival.sunnahassistant.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.databinding.ReminderCardViewBinding;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;
import com.thesunnahrevival.sunnahassistant.views.interfaces.OnDeleteReminderListener;
import com.thesunnahrevival.sunnahassistant.views.interfaces.ReminderItemInteractionListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;


public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ViewHolder> {
    public boolean mShowOnBoardingTutorial = false;
    private List<Reminder> mAllReminders = new ArrayList<>();
    private ReminderItemInteractionListener mListener;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private OnDeleteReminderListener mDeleteReminderListener;

    public ReminderListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(mContext);

        ReminderCardViewBinding binding = DataBindingUtil.inflate(mLayoutInflater,
                R.layout.reminder_card_view, viewGroup, false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Reminder currentReminder = mAllReminders.get(i);
        viewHolder.bind(currentReminder);

        if (i == 1 && mShowOnBoardingTutorial) {
            new GuideView.Builder(mContext)
                    .setTitle(mContext.getString(R.string.edit_reminder))
                    .setContentText(mContext.getString(R.string.edit_reminder_description))
                    .setGravity(Gravity.auto)
                    .setDismissType(DismissType.anywhere)
                    .setTargetView(viewHolder.binding.cardView)
                    .setContentTextSize(16)
                    .setTitleTextSize(20)
                    .build()
                    .show();
            mShowOnBoardingTutorial = false;
        }
    }

    @Override
    public int getItemCount() {
        return mAllReminders.size();
    }

    public void setData(List<Reminder> data) {
        mAllReminders = data;
        notifyDataSetChanged();
    }

    /**
     * @param listener the listener that will handle when a RecyclerView item is clicked
     */
    public void setOnItemClickListener(ReminderItemInteractionListener listener) {
        mListener = listener;
    }

    public Context getContext() {
        return mContext;
    }

    public void setDeleteReminderListener(OnDeleteReminderListener deleteReminderListener) {
        mDeleteReminderListener = deleteReminderListener;
    }

    public void deleteReminder(int position) {
        mDeleteReminderListener.deleteReminder(position);
    }

    /**
     * Inner Class
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private ReminderCardViewBinding binding;

        ViewHolder(@NonNull ReminderCardViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        private void bind(Reminder reminder) {
            binding.setReminder(reminder);
            binding.reminderTime.setText(TimeDateUtil.formatTimeInMilliseconds(mContext, reminder.getTimeInMilliSeconds()));
            binding.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) ->
                    mListener.onToggleButtonClick(buttonView, isChecked, reminder));
            binding.cardView.setOnClickListener((view) -> mListener.openBottomSheet(view, reminder));

        }
    }
}
