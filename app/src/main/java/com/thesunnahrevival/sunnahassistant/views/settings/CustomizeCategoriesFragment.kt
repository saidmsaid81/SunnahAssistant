package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentCustomizeCategoriesBinding
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.CategoriesSettingsAdapter
import com.thesunnahrevival.sunnahassistant.views.dialogs.AddCategoryDialogFragment
import java.util.*

class CustomizeCategoriesFragment : SunnahAssistantFragment(),
    CategoriesSettingsAdapter.DeleteCategoryListener {

    private lateinit var mBinding: FragmentCustomizeCategoriesBinding
    private val deletedCategories = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_customize_categories, container, false
        )

        mViewModel.getSettings().observe(viewLifecycleOwner) {
            mViewModel.settingsValue = it
            mBinding.settings = it
            if (it != null) {
                mBinding.categoriesList.adapter = it.categories?.let { categories ->
                    CategoriesSettingsAdapter(
                        categories,
                        this
                    )
                }
            }

        }


        mBinding.fab.setOnClickListener {
            val addCategoryDialogFragment = AddCategoryDialogFragment()
            addCategoryDialogFragment.show(
                requireActivity().supportFragmentManager, "dialog"
            )
        }


        return mBinding.root
    }

    override fun deleteToDoCategory(categoriesList: TreeSet<String>, category: String) {
        val prayer = resources.getStringArray(R.array.categories)[2]
        val uncategorized = resources.getStringArray(R.array.categories)[0]
        val deleteInfo: String
        if (!category.matches(uncategorized.toRegex()) &&
            !category.matches(prayer.toRegex())
        ) {
            deleteInfo = getString(R.string.confirm_delete_category, category)
            categoriesList.remove(category)
            deletedCategories.add(category)
            mViewModel.settingsValue?.categories = categoriesList
            mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
        } else
            deleteInfo = getString(R.string.category_cannot_be_deleted, category)

        val snackBar = Snackbar.make(
            mBinding.root,
            deleteInfo,
            Snackbar.LENGTH_LONG
        )

        if (!category.matches(uncategorized.toRegex()) &&
            !category.matches(prayer.toRegex())
        )
            snackBar.setAction(R.string.undo_delete) {
                categoriesList.add(category)
                deletedCategories.remove(category)
                mViewModel.settingsValue?.categories = categoriesList
                mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
            }

        snackBar.show()

    }

    override fun onPause() {
        super.onPause()
        mViewModel.updatedDeletedCategories(deletedCategories)
    }
}