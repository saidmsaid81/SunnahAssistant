package com.thesunnahrevival.sunnahassistant.views

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.CategoriesSettingsBinding
import com.thesunnahrevival.sunnahassistant.databinding.NotificationSettingsBinding
import com.thesunnahrevival.sunnahassistant.databinding.PrayerTimeSettingsBinding
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService
import com.thesunnahrevival.sunnahassistant.utilities.NotificationUtil
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.CategoriesSettingsAdapter

open class SettingsFragmentListeners: Fragment(), AdapterView.OnItemClickListener, View.OnClickListener,  PopupMenu.OnMenuItemClickListener, CompoundButton.OnCheckedChangeListener, CategoriesSettingsAdapter.DeleteCategoryListener {

    lateinit var mViewModel : SettingsViewModel
    lateinit var mBinding : ViewDataBinding
    private val deletedCategories = arrayListOf<String>()

    private fun showPopup(arrayResourceId: Int, viewId: Int, id: Int ) {
        val asrCalculationMethods = resources.getStringArray(arrayResourceId)
        val popupMenu = PopupMenu(context, activity?.findViewById(viewId))
        for ((index, method) in asrCalculationMethods.withIndex()) {
            popupMenu.menu.add(id, Menu.NONE, index, method)
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val myActivity = activity
        if (myActivity != null){
            val transaction = myActivity.supportFragmentManager.beginTransaction()
            when(position) {
                0 -> transaction.replace(R.id.settings_container, SettingsFragment.newInstance(R.layout.hijri_date_settings))
                1 -> transaction.replace(R.id.settings_container, SettingsFragment.newInstance(R.layout.prayer_time_settings))
                2 -> transaction.replace(R.id.settings_container, SettingsFragment.newInstance(R.layout.categories_settings))
                3 -> transaction.replace(R.id.settings_container, SettingsFragment.newInstance(R.layout.notification_settings))
                4 -> transaction.replace(R.id.settings_container, SettingsFragment.newInstance(R.layout.display_settings))
                5 -> {
                    val intent = SunnahAssistantUtil.generateEmailIntent()
                    if (intent.resolveActivity(myActivity.packageManager) != null)
                        startActivity(intent)
                    else
                        Toast.makeText(context, getString(R.string.no_email_app_installed), Toast.LENGTH_LONG).show()
                }
            }

            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.location_details -> {
                val dialogFragment = EnterLocationDialogFragment()
                fragmentManager?.let { dialogFragment.show(it, "dialog") }
            }
            R.id.calculation_details ->
                showPopup(R.array.calculation_methods, R.id.calculation_method, R.id.calculation_details)
            R.id.asr_calculation_details ->
                showPopup(R.array.asr_juristic_method, R.id.asr_calculation_method, R.id.asr_calculation_details)
            R.id.higher_latitude_details ->
                showPopup(R.array.latitude_options, R.id.higher_latitude_method, R.id.higher_latitude_details)
            R.id.layout_settings -> showPopup(R.array.layout_options, R.id.layout, R.id.layout_settings)
            R.id.theme_settings -> showPopup(R.array.theme_options, R.id.theme, R.id.theme_settings)
            R.id.fab -> {
                fragmentManager?.let {
                    val addCategoryDialogFragment = AddCategoryDialogFragment()
                    addCategoryDialogFragment.show(it, "dialog")
                }
            }
            R.id.notification_vibration_settings ->
                showPopup(R.array.vibration_options, R.id.vibration, R.id.notification_vibration_settings)
            R.id.notification_priority_settings ->
                showPopup(R.array.priority_options, R.id.importance, R.id.notification_priority_settings)
            R.id.notification_tone_settings -> {
                val currentRingtone : Uri? = mViewModel.settings.value?.notificationToneUri
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtone)
                startActivityForResult(intent, 1)
            }

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val calculationMethods = resources.getStringArray(R.array.calculation_methods)
        val asrMethods = resources.getStringArray(R.array.asr_juristic_method)
        val latitudeMethods = resources.getStringArray(R.array.latitude_options)
        when(item?.groupId){
            R.id.calculation_details ->
                mViewModel.updateCalculationMethod(calculationMethods.indexOf(item.title.toString()) + 1)
            R.id.asr_calculation_details -> mViewModel.updateAsrCalculationMethod(asrMethods.indexOf(item.title.toString()))
            R.id.higher_latitude_details -> mViewModel.updateHigherLatitudeMethod(latitudeMethods.indexOf(item.title.toString()) + 1)
            R.id.layout_settings -> mViewModel.updateLayout(item.title.toString().matches("Expanded View".toRegex()))
            R.id.theme_settings -> {
                if (item.title.toString().matches("Light".toRegex())) {
                    mViewModel.updateTheme(true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    mViewModel.updateTheme(false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
            R.id.notification_vibration_settings ->
                mViewModel.updateVibrationSettings(item.title.toString().matches("On".toRegex()))
            R.id.notification_priority_settings -> mViewModel.updateNotificationPriority(item.order + 1)
        }

        return true
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView?.isPressed == true) {
            when (buttonView.id) {
                R.id.display_hijri_date_switch -> mViewModel.updateDisplayHijriDateSettings(isChecked)
                R.id.activate_prayer_time_alerts -> mViewModel.updateAutomaticPrayerTimeSettings(isChecked)
                R.id.next_reminder_sticky_settings -> {
                    if (isChecked)
                        activity?.startService(Intent(activity, NextReminderService::class.java))
                    else
                        activity?.stopService(Intent(activity, NextReminderService::class.java))
                    mViewModel.showStickyNotification(isChecked)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            mViewModel.updateRingtone(data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI))
        }
    }

    override fun onStop() {
        super.onStop()
        when (mBinding) {
            is PrayerTimeSettingsBinding -> mViewModel.updatePrayerTimesData()
            is CategoriesSettingsBinding -> mViewModel.updatedDeletedCategories(deletedCategories)
            is NotificationSettingsBinding -> {
                if (mViewModel.isSettingsUpdated){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationUtil.deleteReminderNotificationChannel(context)
                        mViewModel.settings.value?.let {
                            NotificationUtil.createReminderNotificationChannel(
                                    context, it.notificationToneUri, it.isVibrate, it.priority)
                        }
                    }
                }
            }
        }
    }

    override fun deleteReminderCategory(categoriesList: HashSet<String>, category: String) {
        val deleteInfo :String
        if (!category.matches(SunnahAssistantUtil.UNCATEGORIZED.toRegex()) && !category.matches(SunnahAssistantUtil.PRAYER.toRegex()) ) {
            deleteInfo = getString(R.string.confirm_delete_category, category)
            categoriesList.remove(category)
            deletedCategories.add(category)
            mViewModel.updateCategories(categoriesList)
        }
        else
            deleteInfo = getString(R.string.category_cannot_be_deleted, category)

        val snackBar = Snackbar.make((mBinding as CategoriesSettingsBinding).root,
                deleteInfo,
                Snackbar.LENGTH_LONG)

        if (!category.matches(SunnahAssistantUtil.UNCATEGORIZED.toRegex()) && !category.matches(SunnahAssistantUtil.PRAYER.toRegex()))
            snackBar.setAction(R.string.undo_delete) {
                categoriesList.add(category)
                deletedCategories.remove(category)
                mViewModel.updateCategories(categoriesList)
            }

        snackBar.show()
    }
}