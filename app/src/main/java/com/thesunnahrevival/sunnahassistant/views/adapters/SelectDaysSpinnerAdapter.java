package com.thesunnahrevival.sunnahassistant.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.SelectDays;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class SelectDaysSpinnerAdapter extends ArrayAdapter<SelectDays> {

    private Context mContext;
    private ArrayList<SelectDays> nameOfDays;
    private ArrayList<String> checkedDays;
    private boolean isFromView = false;

    public SelectDaysSpinnerAdapter(Context context, int resource, ArrayList<SelectDays> nameOfDays, ArrayList<String> checkedDays) {
        super(context, resource, nameOfDays);
        this.mContext = context;
        this.nameOfDays = nameOfDays;
        this.checkedDays = new ArrayList<>(checkedDays);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public ArrayList<String> getCheckedDays() {
        return checkedDays;
    }

    private View getCustomView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.select_days_spinner_item, parent, false);
            holder = new ViewHolder();
            holder.mTextView = convertView.findViewById(R.id.days_text);
            holder.mCheckBox = convertView.findViewById(R.id.days_checkbox);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTextView.setText(nameOfDays.get(position).getTitle());

        // To check whether checked event fire from getview() or user input
        isFromView = true;
        holder.mCheckBox.setChecked(nameOfDays.get(position).isSelected());
        isFromView = false;

        if ((position == 0)) {
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }
        holder.mCheckBox.setTag(position);
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isFromView && isChecked) {
                nameOfDays.get(position).setSelected(true);
                String shortNameDay = nameOfDays.get(position).getTitle().substring(0, 3);
                checkedDays.add(shortNameDay);
            } else if (!isFromView) {
                nameOfDays.get(position).setSelected(false);
                String shortNameDay = nameOfDays.get(position).getTitle().substring(0, 3);
                checkedDays.remove(shortNameDay);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView mTextView;
        private CheckBox mCheckBox;
    }
}



