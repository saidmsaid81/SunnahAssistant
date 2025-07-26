package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.thesunnahrevival.sunnahassistant.data.model.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails

@Dao
interface AyahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayah: Ayah)

    @Transaction
    @Query("SELECT * FROM ayahs a WHERE a.id = :ayahId ")
    suspend fun getFullAyahDetailsById(ayahId: Int): FullAyahDetails?

    @Transaction
    @Query(
        "SELECT * FROM ayahs a " +
                "WHERE a.id IN (SELECT l.ayah_id FROM lines l WHERE l.page_number = :pageNumber) "
    )
    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int): List<FullAyahDetails>

    @Update
    suspend fun updateAyah(ayah: Ayah)

    @Query("UPDATE ayahs SET bookmarked = :bookmarked WHERE id = :ayahId")
    suspend fun updateAyahBookmarkStatus(ayahId: Int, bookmarked: Boolean)
}