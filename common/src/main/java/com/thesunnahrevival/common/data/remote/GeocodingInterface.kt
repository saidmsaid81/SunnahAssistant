package com.thesunnahrevival.common.data.remote

import com.thesunnahrevival.common.data.model.GeocodingData
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingInterface {
    @GET("maps/api/geocode/json")
    suspend fun getGeocodingData(
            @Query("address") address: String,
            @Query("key") apiKey: String,
            @Query("language") language: String
    ): GeocodingData?
}