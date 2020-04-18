package com.thesunnahrevival.sunnahassistant.views;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

public class AddCategoryDialogFragment extends DialogFragment {

    public static MutableLiveData<String> category = new MutableLiveData<>();
    private SettingsViewModel mSettingsViewModel;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mSettingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.add_category_dialog_layout, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, (dialog, id) -> {
                    ArrayList<String> categories;

                    AppSettings settings = mSettingsViewModel.getSettings().getValue();
                    if ( settings != null){
                        categories = settings.getCategories();
                        EditText categoryEditText = view.findViewById(R.id.category);
                        categories.remove("+ Create New Category");
                        categories.add(categoryEditText.getText().toString());
                        category.setValue(categoryEditText.getText().toString());
                        mSettingsViewModel.updateCategories(categories);
                    }


                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
                .setTitle("Add New Category");
        return builder.create();
    }
}
