package com.thesunnahrevival.sunnahassistant.views.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.CategoriesSettingsBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.adapters.CategoriesSettingsAdapter
import com.thesunnahrevival.sunnahassistant.views.dialogs.AddCategoryDialogFragment
import java.util.*

class CustomizeCategoriesFragment: Fragment(), CategoriesSettingsAdapter.DeleteCategoryListener {

    private lateinit var mViewModel: SunnahAssistantViewModel
    private lateinit var mBinding: CategoriesSettingsBinding
    private val deletedCategories = arrayListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.categories_settings, container, false)

        val myActivity = activity
        if (myActivity != null) {

            mViewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            mViewModel.getSettings().observe(viewLifecycleOwner, Observer {
                mViewModel.settingsValue = it
                mBinding.settings = it
                if (it != null) {
                    mBinding.categoriesList.adapter = it.categories?.let { categories -> CategoriesSettingsAdapter(categories, this) }
                }

            })


            mBinding.fab.setOnClickListener{
                fragmentManager?.let {
                    val addCategoryDialogFragment = AddCategoryDialogFragment()
                    addCategoryDialogFragment.show(it, "dialog")
                }
            }
        }

        return mBinding.root
    }

    override fun deleteReminderCategory(categoriesList: TreeSet<String>, category: String) {
        val prayer = resources.getStringArray(R.array.categories)[2]
        val uncategorized = resources.getStringArray(R.array.categories)[0]
        val deleteInfo :String
        if (!category.matches(uncategorized.toRegex()) &&
                !category.matches(prayer.toRegex()) )
        {
            deleteInfo = getString(R.string.confirm_delete_category, category)
            categoriesList.remove(category)
            deletedCategories.add(category)
            mViewModel.settingsValue?.categories = categoriesList
            mViewModel.settingsValue?.let { mViewModel.updateSettings(it) }
        }
        else
            deleteInfo = getString(R.string.category_cannot_be_deleted, category)

        val snackBar = Snackbar.make(mBinding.root,
                deleteInfo,
                Snackbar.LENGTH_LONG)

        if (!category.matches(uncategorized.toRegex()) &&
                !category.matches(prayer.toRegex()))
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
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}