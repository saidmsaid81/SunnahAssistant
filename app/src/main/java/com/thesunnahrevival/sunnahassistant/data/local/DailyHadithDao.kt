package com.thesunnahrevival.sunnahassistant.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith

@Dao
interface DailyHadithDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyHadithList(hadithList : List<DailyHadith>)

    @Query(
        "SELECT * FROM daily_hadith ORDER BY pubDateMilliseconds DESC "
    )
    fun getDailyHadithList(): PagingSource<Int, DailyHadith>

    @Query("SELECT EXISTS (SELECT id FROM daily_hadith WHERE pubDateMilliseconds = :todaysDateId )")
    suspend fun isTodaysHadithLoaded(todaysDateId : Long): Boolean
}