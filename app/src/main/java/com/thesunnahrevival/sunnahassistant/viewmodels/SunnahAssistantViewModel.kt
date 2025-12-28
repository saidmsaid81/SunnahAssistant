package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.GeocodingData
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.entity.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.repositories.FlagRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository.Companion.getInstance
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.TRACK_READ_SURAH_PREFIX
import com.thesunnahrevival.sunnahassistant.utilities.DB_NAME
import com.thesunnahrevival.sunnahassistant.utilities.DB_NAME_TEMP
import com.thesunnahrevival.sunnahassistant.utilities.Encryption
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY
import com.thesunnahrevival.sunnahassistant.utilities.RETRY_AFTER_KEY
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORTED_LOCALES
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORT_EMAIL
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.utilities.generateLocalDatefromDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import javax.crypto.BadPaddingException
import kotlin.math.roundToLong


class SunnahAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: SunnahAssistantRepository = getInstance(application)
    private val flagRepository: FlagRepository = FlagRepository.getInstance(application)

    private val resourcesRepository: ResourcesRepository = ResourcesRepository.getInstance(application)

    private val quranTranslationRepository = QuranTranslationRepository.getInstance(getApplication())

    private val surahRepository: SurahRepository = SurahRepository.getInstance(application)

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
    val statusBarHeight = MutableLiveData(0)
    val navBarHeight = MutableLiveData(0)

    private val _selectedAyahId: MutableLiveData<Int?> = MutableLiveData()
    val selectedAyahId: LiveData<Int?> = _selectedAyahId

    private var _currentQuranPage: Int? = null
    val selectedSurah: MutableLiveData<Surah> = MutableLiveData()

    var quranBookmarksSelectedTabIndex by mutableIntStateOf(0)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            resourcesRepository.prepopulateResourcesData()
            val todayDate = LocalDate.now().toString()
            flagRepository.clearFlagsMatching(
                Regex("$TRACK_READ_SURAH_PREFIX-(?!$todayDate).*")
            )
        }
    }

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
                refreshScheduledReminders()
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
                refreshScheduledReminders()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun deleteListOfToDos(toDos: List<ToDo>) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.deleteListOfToDos(toDos)
            withContext(Dispatchers.Main) {
                refreshScheduledReminders()
                triggerCalendarUpdate.value = true
            }
        }
    }

    fun thereToDosOnDay(dayOfWeek: String, dayOfMonth: Int, month: Int, year: Int): Boolean {
        val excludePrayer = getContext().getString(R.string.prayer)
        return mRepository.thereToDosOnDay(excludePrayer, dayOfWeek, dayOfMonth, month, year)
    }

    fun getToDoLiveData(id: Int) = mRepository.getToDoLiveData(id)

    suspend fun getToDoById(id: Int) = mRepository.getToDoById(id)

    fun getTemplateToDoIds() = mRepository.getTemplateToDoIds()

    fun getMalformedToDos() = mRepository.getMalformedToDos()

    fun getIncompleteToDos(): LiveData<PagingData<ToDo>> {
        return mutableReminderParameters.switchMap { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getIncompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun getCompleteToDos(): LiveData<PagingData<ToDo>> {
        return mutableReminderParameters.switchMap { (dateOfReminders, category) ->
            Pager(
                PagingConfig(15),
                pagingSourceFactory = {
                    mRepository.getCompleteToDosOnDay(Date(dateOfReminders), category)
                }
            ).liveData.cachedIn(viewModelScope)
        }
    }

    fun getSunriseTime(): Long? {
        val settings = settingsValue
        if (settings != null) {
            return mRepository.getSunriseTime(
                settings.latitude.toDouble(),
                settings.longitude.toDouble(),
                settings.calculationMethod,
                settings.asrCalculationMethod,
                settings.latitudeAdjustmentMethod,
                selectedToDoDate.dayOfMonth,
                selectedToDoDate.month.ordinal,
                selectedToDoDate.year
            )
        }
        return null

    }

    fun updatePrayerTimeDetails(oldPrayerDetails: ToDo, newPrayerDetails: ToDo) {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.updatePrayerDetails(oldPrayerDetails, newPrayerDetails)
            withContext(Dispatchers.Main) {
                refreshScheduledReminders()
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
        val tooManyRequestString = getContext().getString(R.string.too_many_requests)
        val anErrorOccurred = getContext().getString(R.string.an_error_occurred, SUPPORT_EMAIL)
        val pleaseUpdateApp = getContext().getString(R.string.update_app)

        viewModelScope.launch(Dispatchers.IO) {
            val message: String
            val clientRetryFromTimeMilliseconds = flagRepository.getLongFlag(RETRY_AFTER_KEY) ?: 0

            if (System.currentTimeMillis() < clientRetryFromTimeMilliseconds) {
                message = String.format(
                    tooManyRequestString,
                    formatTimeInMilliseconds(getContext(), clientRetryFromTimeMilliseconds)
                )
            } else {
                val response =
                    mRepository.getGeocodingData(address, settingsValue?.language ?: "en")
                val data = response.body()

                when {
                    listOf(401, 404, 500).contains(response.code()) -> message = anErrorOccurred
                    response.code() == 429 -> {
                        //Too many requests. At the time of writing only 15 requests were allowed per hour.
                        // Check the backend code here https://github.com/saidmsaid81/Sunnah-Assistant-Backend
                        val oneHourInMilliseconds = (60 * 60 * 1000).toLong()
                        var clientRetryFromTimeMilliseconds =
                            System.currentTimeMillis() + oneHourInMilliseconds
                        response.headers().get("Retry-After")?.let {
                            val retryAfterMilliSecondsHeader =
                                (it.toFloatOrNull()?.times(1000))?.roundToLong()
                                    ?: oneHourInMilliseconds
                            clientRetryFromTimeMilliseconds =
                                System.currentTimeMillis() + retryAfterMilliSecondsHeader

                            flagRepository.setFlag(RETRY_AFTER_KEY, clientRetryFromTimeMilliseconds)
                        }
                        message = String.format(
                            tooManyRequestString,
                            formatTimeInMilliseconds(getContext(), clientRetryFromTimeMilliseconds),
                            SUPPORT_EMAIL
                        )
                    }

                    response.code() == 426 -> message = pleaseUpdateApp
                    data != null && data.results.isNotEmpty() -> {
                        updateLocationDetails(data)
                        message = "Successful"
                    }
                    data != null -> {
                        message = if (data.status == "ZERO_RESULTS") {
                            unavailableLocationString
                        } else {
                            serverError
                        }
                    }

                    else -> {
                        message = noNetworkString
                    }
                }
            }

            withContext(Dispatchers.Main) {
                messages.value = message
            }
        }
    }

    suspend fun getNotificationPermissionRequestsCount(): Long {
        return flagRepository.getLongFlag(NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY) ?: 0;
    }

    fun incrementNotificationPermissionRequestsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            var notificationPermissionRequestCount =
                flagRepository.getLongFlag(NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY) ?: 0
            if (notificationPermissionRequestCount != -1L) {
                flagRepository.setFlag(
                    NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY,
                    ++notificationPermissionRequestCount
                )
            }
        }
    }

    fun hideFixNotificationsBanner() {
        viewModelScope.launch(Dispatchers.IO) {
            flagRepository.setFlag(NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY, -1)
        }
    }

    fun snoozeNotification(id: Int, timeInMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val toDo = getToDoById(id) ?: return@launch
            if (settingsValue == null)
                settingsValue = getAppSettingsValue()
            val settings = settingsValue ?: return@launch

            val notificationManager =
                getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (toDo.isAutomaticPrayerTime() &&
                settings.doNotDisturbMinutes > 0 &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                notificationManager.isNotificationPolicyAccessGranted
            ) {
                //Disable the already enabled dnd. It will be re-enabled
                // when snoozed notification triggers
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }

            ReminderManager.getInstance().scheduleReminder(
                getContext(),
                getContext().getString(R.string.reminder),
                mapOf(Pair(id, "${getContext().getString(R.string.snooze)}: ${toDo.name}")),
                mapOf(Pair(id, toDo.category ?: "Uncategorized")),
                System.currentTimeMillis() + (timeInMinutes * 60 * 1000),
                settings.notificationToneUri ?: RingtoneManager.getActualDefaultRingtoneUri(
                    getContext(), RingtoneManager.TYPE_NOTIFICATION
                ),
                settings.isVibrate,
                settings.doNotDisturbMinutes,
                useReliableAlarms = settings.useReliableAlarms,
                calculateDelayFromMidnight = false,
                isSnooze = true
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.notification_successfully_snoozed),
                    Toast.LENGTH_SHORT
                ).show()
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

    fun refreshScheduledReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            ReminderManager.getInstance().refreshScheduledReminders(getContext())
        }
    }

    private fun getContext() = getApplication<Application>().applicationContext

    val isDeviceOffline: Boolean
        get() {
            val connectivityManager = getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                activeNetwork == null || networkCapabilities == null ||
                        !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                connectivityManager.activeNetworkInfo == null ||
                        !connectivityManager.activeNetworkInfo!!.isConnected
            }
        }



    fun localeUpdate() {
        if (SUPPORTED_LOCALES.contains(Locale.getDefault().language)) {
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
                    refreshScheduledReminders()
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
                val appSettings = getAppSettingsValue()
                if (appSettings == null) {
                    undoFailedRestore(databaseFile, existingDataTempFile)
                    return@withContext Pair(
                        null,
                        ""
                    )
                } else {
                    appSettings.notificationToneUri =
                        RingtoneManager.getActualDefaultRingtoneUri(
                            getContext(), RingtoneManager.TYPE_NOTIFICATION
                        )
                    updateSettings(appSettings)
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

    fun setSelectedAyahId(ayahId: Int?) {
        _selectedAyahId.value = ayahId
    }

    fun refreshSelectedAyahId() {
        _selectedAyahId.value = _selectedAyahId.value
    }

    fun nextAyah() {
        val nextAyahId = selectedAyahId.value?.plus(1)

        nextAyahId?.let {
            _selectedAyahId.value = it
        }
    }

    fun previousAyah() {
        val previousAyahId = selectedAyahId.value?.minus(1)
        previousAyahId?.let {
            _selectedAyahId.value = it
        }
    }

    suspend fun toggleAyahBookmark(ayah: Ayah, updateSelectedAyahId: Boolean = false) {
        quranTranslationRepository.toggleAyahBookmarkStatus(ayah.id)
        if (updateSelectedAyahId) {
            withContext(Dispatchers.Main) {
                _selectedAyahId.value = ayah.id
            }
        }
    }

    fun getCurrentQuranPage() = _currentQuranPage ?: 1

    fun updateCurrentPage(page: Int, updateLastReadPage: Boolean = true) {
        _currentQuranPage = page

        viewModelScope.launch(Dispatchers.IO) {
            val surah = surahRepository.getSurahByPage(page)
            if (updateLastReadPage) {
                settingsValue?.let {
                    it.lastReadPage = page
                    mRepository.updateAppSettings(it)
                }
            }
            surah?.let {
                withContext(Dispatchers.Main) {
                    selectedSurah.value = it
                }
            }
        }
    }
}