package com.thesunnahrevival.common.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.databinding.CategoriesSettingsBinding
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SunnahAssistantFragment
import com.thesunnahrevival.common.views.adapters.CategoriesSettingsAdapter
import com.thesunnahrevival.common.views.dialogs.AddCategoryDialogFragment
import java.util.*

class CustomizeCategoriesFragment : SunnahAssistantFragment(),
    CategoriesSettingsAdapter.DeleteCategoryListener {

    private lateinit var mBinding: CategoriesSettingsBinding
    private val deletedCategories = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.categories_settings, container, false
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

    override fun deleteReminderCategory(categoriesList: TreeSet<String>, category: String) {
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

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundle
        )
    }
}