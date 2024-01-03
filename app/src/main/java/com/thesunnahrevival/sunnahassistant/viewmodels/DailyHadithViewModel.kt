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
import com.thesunnahrevival.sunnahassistant.data.DailyHadithRepository
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DailyHadithViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepository: DailyHadithRepository =
        DailyHadithRepository.getInstance(application)

    val showDailyHadithLoadingIndicator = MutableLiveData(true)

    fun getDailyHadithList(): LiveData<PagingData<DailyHadith>> {
        viewModelScope.launch(Dispatchers.IO) {
            mRepository.fetchHadith()
            withContext(Dispatchers.Main) {
                showDailyHadithLoadingIndicator.value = false
            }
        }
        return Pager(
            PagingConfig(3),
            pagingSourceFactory = {
                mRepository.getDailyHadithFromTheSunnahRevivalBlog()
            }
        ).liveData.cachedIn(viewModelScope)
    }
}