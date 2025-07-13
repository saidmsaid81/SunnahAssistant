package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.thesunnahrevival.sunnahassistant.data.repositories.DailyHadithRepository
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DailyHadithViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepository: DailyHadithRepository =
        DailyHadithRepository.getInstance(application)

    val dailyHadithFetchingStatus = MutableLiveData<DailyHadithRepository.DailyHadithFetchingStatus?>(null)

    fun getDailyHadithList(): LiveData<PagingData<DailyHadith>> {
        return Pager(
            PagingConfig(3),
            pagingSourceFactory = {
                mRepository.getDailyHadithFromTheSunnahRevivalBlog()
            }
        ).liveData.cachedIn(viewModelScope)
    }

    fun fetchDailyHadith() {
        viewModelScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                dailyHadithFetchingStatus.value = DailyHadithRepository.DailyHadithFetchingStatus.LOADING
            }

            val fetchDailyHadithSatus = mRepository.fetchDailyHadith()

            withContext(Dispatchers.Main) {
                dailyHadithFetchingStatus.value = fetchDailyHadithSatus
            }
        }
    }
}