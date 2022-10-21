package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.sunnahassistant.data.local.DB_NAME
import com.thesunnahrevival.sunnahassistant.data.local.DB_NAME_TEMP
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.services.NextToDoService
import com.thesunnahrevival.sunnahassistant.utilities.Encryption
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import com.thesunnahrevival.sunnahassistant.utilities.generateLocalDatefromDate
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDate
import java.util.*
import javax.crypto.BadPaddingException


class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)
    private val mutex = Mutex()

    var selectedToDo =
        ToDo(
            name = "", frequency = Frequency.OneTime,
            category = application.resources.getStringArray(R.array.categories)[0], //Uncategorized
            day = LocalDate.now().dayOfMonth,
            month = LocalDate.now().month.ordinal,
            year = LocalDate.now().year
        )
    var isToDoTemplate = false

    var settingsValue: AppSettings? = null
    val messages = MutableLiveData<String>()
    var isPrayerSettingsUpdated = false
    private val mutableReminderParameters =
        MutableLiveData(Pair(System.currentTimeMillis(), ""))

    val selectedToDoDate: LocalDate
        get() {
            return generateLocalDatefromDate(
                Date(
                    mutableReminderParameters.value?.first ?: System.currentTimeMillis()
                )
            )
        }
    val categoryToDisplay: String
        get() {
            return mutableReminderParameters.value?.second ?: ""
        }

    val triggerCalendarUpdate = MutableLiveData<Boolean>()

    fun setToDoParameters(date: Long? = null, category: String? = null) {
        val currentDateParameter =
            mutableReminderParameters.value?.first ?: System.currentTimeMillis()
        val currentCategoryParameter = mutableReminderParameters.value?.second ?: ""
        mutableReminderParameters.value =
            Pair(date ?: currentDateParameter, category ?: currentCategoryParameter)
    }

    fun insertToDo(toDo: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.insertToDo(toDo)
            withContext(Dispatchers.Main) {
                startService()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun updateToDo(toDo: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updateToDo(toDo)
        }
    }

    fun deleteToDo(toDo: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteToDo(toDo)
            withContext(Dispatchers.Main) {
                startService()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun deleteListOfToDos(toDos: List<ToDo>) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteListOfToDos(toDos)
            withContext(Dispatchers.Main) {
                startService()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun thereToDosOnDay(dayOfWeek: String, dayOfMonth: Int, month: Int, year: Int): Boolean {
        val excludePrayer = getContext().getString(R.string.prayer)
        return mRepository.thereToDosOnDay(excludePrayer, dayOfWeek, dayOfMonth, month, year)
    }

    fun getToDo(id: Int) = mRepository.getToDo(id)

    fun getTemplateToDoIds() = mRepository.getTemplateToDoIds()

    fun getMalformedToDos() = mRepository.getMalformedToDos()

    fun getIncompleteToDos(): LiveData<PagingData<ToDo>> {
        return Transformations.switchMap(mutableReminderParameters) { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getIncompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun getCompleteToDos(): LiveData<PagingData<ToDo>> {
        return Transformations.switchMap(mutableReminderParameters) { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getCompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun updatePrayerTimeDetails(oldPrayerDetails: ToDo, newPrayerDetails: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updatePrayerDetails(oldPrayerDetails, newPrayerDetails)
            withContext(Dispatchers.Main) {
                startService()
            }
        }
    }

    fun updatePrayerTimesData() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = settingsValue
            if (settings?.formattedAddress?.isNotBlank() == true) {
                if (settings.isAutomaticPrayerAlertsEnabled) {
                    mRepository.updatePrayerReminders(
                        getContext().resources.getStringArray(R.array.prayer_names),
                        getContext().resources.getStringArray(R.array.categories)[2]
                    )
                } else
                    mRepository.deletePrayerTimesData()
                updateSettings(settings)
            }
        }
    }

    fun updatedDeletedCategories(deletedCategories: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (deletedCategories.isNotEmpty()) {
                mRepository.updateDeletedCategories(
                    deletedCategories,
                    getContext().resources.getStringArray(R.array.categories)[0]
                )
            }
        }
    }

    fun getSettings() = mRepository.getAppSettings().asLiveData()

    suspend fun getAppSettingsValue(): AppSettings? {
        return mRepository.getAppSettingsValue()
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updateAppSettings(settings)
        }
    }

    fun getGeocodingData(address: String) {
        val unavailableLocationString =
            getContext().getString(R.string.unavailable_location)
        val serverError: String =
            getContext().getString(R.string.server_error_occured)
        val noNetworkString =
            getContext().getString(R.string.error_updating_location)

        viewModelScope.launch(Dispatchers.IO) {
            val data = mRepository.getGeocodingData(address, settingsValue?.language ?: "en")
            val message: String

            when {
                data != null && data.results.isNotEmpty() -> {
                    updateLocationDetails(data)
                    message = "Successful"
                }
                data != null -> {
                    if (data.status == "ZERO_RESULTS")
                        message = unavailableLocationString
                    else {
                        message = serverError
                        mRepository.reportGeocodingServerError(data.status)
                    }

                }
                else -> {
                    message = noNetworkString
                }
            }

            withContext(Dispatchers.Main) {
                messages.value = message
            }
        }
    }

    private fun updateLocationDetails(data: GeocodingData) {
        val tempSettings = settingsValue
        val result = data.results[0]

        if (tempSettings != null) {
            tempSettings.formattedAddress = result.formattedAddress
            tempSettings.latitude = result.geometry.location.lat
            tempSettings.longitude = result.geometry.location.lng
            updateSettings(tempSettings)
            isPrayerSettingsUpdated = true
        }
    }

    private fun startService() {
        getApplication<Application>().startService(
            Intent(
                getApplication(),
                NextToDoService::class.java
            )
        )
    }

    private fun getContext() = getApplication<Application>().applicationContext

    val isDeviceOffline: Boolean
        get() {
            val connectivityManager = getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo == null ||
                    !connectivityManager.activeNetworkInfo!!.isConnected
        }


    fun localeUpdate() {
        if (supportedLocales.contains(Locale.getDefault().language)) {
            val configuration =
                Configuration(getContext().resources.configuration)
            configuration.setLocale(Locale(settingsValue?.language ?: "en"))

            val oldCategoryNames = getContext()
                .createConfigurationContext(configuration)
                .resources
                .getStringArray(R.array.categories)

            val newCategoryNames = mutableListOf<String>()
            newCategoryNames.addAll((getContext().resources.getStringArray(R.array.categories)))

            val oldPrayerNames = getContext()
                .createConfigurationContext(configuration)
                .resources
                .getStringArray(R.array.prayer_names)
            val newPrayerNames = getContext().resources.getStringArray(R.array.prayer_names)

            viewModelScope.launch(Dispatchers.IO) {
                for ((index, oldCategoryName) in oldCategoryNames.withIndex()) {
                    mRepository.updateCategory(oldCategoryName, newCategoryNames[index])
                }

                mRepository.updatePrayerNames(oldPrayerNames, newPrayerNames)

                settingsValue?.language = Locale.getDefault().language
                settingsValue?.categories?.removeAll(oldCategoryNames.toSet())
                settingsValue?.categories?.addAll(newCategoryNames)
                settingsValue?.let {
                    updateSettings(it)
                }

                withContext(Dispatchers.Main) {
                    startService()
                }
            }
        }
    }


    fun getTemplateToDos() = TemplateToDos().getTemplateToDos(getContext())

    fun backupPlainData(dataUri: Uri?): Pair<Boolean, String> {
        mRepository.closeDB()
        val dbPlainData = getContext().getDatabasePath(DB_NAME).readBytes()
        return doBackup(dataUri, dbPlainData)
    }

    fun backupEncryptedData(dataUri: Uri?, password: String): Pair<Boolean, String> {
        mRepository.closeDB()
        val encryptedData = Encryption().encrypt(
            getContext().getDatabasePath(DB_NAME).readBytes(),
            password
        )
        return doBackup(dataUri, encryptedData)
    }

    private fun doBackup(dataUri: Uri?, inputData: Any): Pair<Boolean, String> {
        return try {
            val outputStream =
                dataUri?.let { getContext().contentResolver.openOutputStream(it) }
                    ?: return Pair(false, "")

            if (inputData is ByteArray) { //Plain Text
                outputStream.use {
                    it.write(inputData)
                }
            } else { //Encrypted
                ObjectOutputStream(outputStream).use {
                    it.writeObject(inputData)
                }
            }

            Pair(true, getContext().getString(R.string.backup_successful))
        } catch (exception: Exception) {
            Log.e("Back up Exception", exception.message.toString())
            Pair(false, getContext().getString(R.string.backup_failed))
        }
    }

    suspend fun restorePlainData(dataUri: Uri?): Pair<Boolean?, String> {
        val backupInputStream =
            dataUri?.let { getContext().contentResolver.openInputStream(it) }
                ?: return Pair(false, "")

        return mutex.withLock {
            doRestore(backupInputStream.readBytes())
        }
    }

    suspend fun restoreEncryptedData(dataUri: Uri?, password: String): Pair<Boolean?, String> {
        val backupInputStream =
            dataUri?.let { getContext().contentResolver.openInputStream(it) }
                ?: return Pair(false, "")

        return withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    ObjectInputStream(backupInputStream).use {
                        when (val data = it.readObject()) {
                            is Map<*, *> -> {
                                if (data.containsKey("iv") && data.containsKey("salt") && data.containsKey(
                                        "encrypted"
                                    )
                                ) {
                                    val iv = data["iv"]
                                    val salt = data["salt"]
                                    val encrypted = data["encrypted"]

                                    val decrypted =
                                        if (iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                                            Encryption().decrypt(
                                                hashMapOf(
                                                    "iv" to iv,
                                                    "salt" to salt,
                                                    "encrypted" to encrypted
                                                ), password
                                            )
                                        } else
                                            null
                                    if (decrypted != null)
                                        return@withContext doRestore(decrypted)


                                }
                            }
                        }
                    }
                    return@withContext Pair(
                        false,
                        getContext().getString(R.string.restore_failed_invalid_file)
                    )
                } catch (exception: Exception) {
                    Log.e("Decryption Exception", exception.toString())
                    when (exception) {
                        is BadPaddingException -> {
                            return@withContext Pair(
                                false,
                                getContext().getString(R.string.restore_failed_incorrect_password)
                            )
                        }
                        else -> return@withContext Pair(
                            false,
                            getContext().getString(R.string.restore_failed_invalid_file)
                        )
                    }
                }
            }
        }
    }

    private suspend fun doRestore(dataToRestore: ByteArray): Pair<Boolean?, String> {
        return withContext(Dispatchers.IO) {
            val existingDataTempFile = getContext().getDatabasePath(DB_NAME_TEMP)
            val databaseFile = getContext().getDatabasePath(DB_NAME)
            try {
                mRepository.closeDB()

                //Create a temp db file which will be used if restoring fails
                val existingDBBytes = databaseFile.readBytes()
                existingDataTempFile.writeBytes(existingDBBytes)
                databaseFile.writeBytes(dataToRestore)
                Pair(false, getContext().getString(R.string.restore_failed))
            } catch (exception: Exception) {
                Log.e("Restore Exception", exception.toString())
                undoFailedRestore(databaseFile, existingDataTempFile)
                Pair(false, getContext().getString(R.string.restore_failed))
            } finally {
                if (getAppSettingsValue() == null) {
                    undoFailedRestore(databaseFile, existingDataTempFile)
                    return@withContext Pair(
                        null,
                        ""
                    )
                } else {
                    existingDataTempFile.delete()
                    return@withContext Pair(
                        true,
                        getContext().getString(R.string.restore_successful)
                    )
                }

            }

        }
    }

    private fun undoFailedRestore(databaseFile: File, existingDataFile: File) {
        mRepository.closeDB()
        databaseFile.writeBytes(existingDataFile.readBytes())
        existingDataFile.delete()
    }
}