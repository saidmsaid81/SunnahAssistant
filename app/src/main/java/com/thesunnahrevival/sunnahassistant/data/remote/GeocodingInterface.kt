package com.thesunnahrevival.sunnahassistant.data.remote

import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
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