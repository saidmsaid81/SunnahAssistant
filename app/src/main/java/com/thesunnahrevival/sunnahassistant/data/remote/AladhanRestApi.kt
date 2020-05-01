package com.thesunnahrevival.sunnahassistant.data.remote

import androidx.lifecycle.MutableLiveData
import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimes
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object AladhanRestApi  {
    private val mAladhanInterface: AladhanInterface
    var errorMessages = MutableLiveData<String>()
    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        mAladhanInterface = retrofit.create(AladhanInterface::class.java)
    }



    fun fetchPrayerTimes(reminderDAO: ReminderDAO, latitude: Float, longitude: Float, month: String?, year: String?, method: Int, asrCalculationMethod: Int, latitudeAdjustmentMethod: Int) {
        errorMessages.value = "Refreshing Prayer Times data..."
        mAladhanInterface.getPrayerTimes(latitude, longitude, month, year, method,
                asrCalculationMethod, latitudeAdjustmentMethod).enqueue(object : Callback<PrayerTimes?> {
            override fun onResponse(call: Call<PrayerTimes?>, response: Response<PrayerTimes?>) {
                if (!response.isSuccessful) {
                    return
                }
                if (response.body() != null) {
                    processPrayerTimesData(response.body(), reminderDAO)
                } else {
                    errorMessages.value = "Error fetching Prayer times data. Please Check Your Internet Connection"
                }
            }

            override fun onFailure(call: Call<PrayerTimes?>, t: Throwable) {
                errorMessages.value = "Error fetching Prayer times data. Please Check Your Internet Connection"
            }
        })
    }

    private fun processPrayerTimesData(prayerTimes: PrayerTimes?, reminderDAO :ReminderDAO) {
        CoroutineScope(Dispatchers.IO).launch {
            val prayerNames = arrayOf("Fajr Prayer", "Dhuhr Prayer", "Asr Prayer", "Maghrib Prayer", "Isha Prayer")
            val listOfPrayerTimes = mutableListOf<Reminder>()
            prayerTimes?.let {
                val rawPrayerTimes = it.data
                for (i in rawPrayerTimes.indices) {
                    val timings = rawPrayerTimes[i].timings
                    val day = rawPrayerTimes[i].date.gregorian.day
                    val fajrTime = timings.fajr.substring(0, 5)
                    val dhuhrTime = timings.dhuhr.substring(0, 5)
                    val asrTime = timings.asr.substring(0, 5)
                    val maghribTime = timings.maghrib.substring(0, 5)
                    val ishaTime = timings.isha.substring(0, 5)
                    val times = arrayOf(fajrTime, dhuhrTime, asrTime, maghribTime, ishaTime)
                    for (j in times.indices) {
                        val reminder = Reminder(
                                prayerNames[j],
                                "",
                                TimeDateUtil.getTimestampInSeconds(times[j]),
                                SunnahAssistantUtil.PRAYER,
                                SunnahAssistantUtil.DAILY,
                                false, day,
                                null, null, 0,
                                null)
                        listOfPrayerTimes.add(reminder)
                    }
                }
            }

            reminderDAO.addRemindersList(listOfPrayerTimes)
            withContext(Dispatchers.Main){
                errorMessages.value = "Successful"
            }
        }
    }

}