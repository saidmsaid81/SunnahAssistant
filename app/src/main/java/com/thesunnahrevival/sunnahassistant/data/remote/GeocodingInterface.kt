package com.thesunnahrevival.sunnahassistant.data.remote

import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingInterface {
    @GET("geocoding-data")
    suspend fun getGeocodingData(
        @Query("address") address: String,
        @Query("language") language: String
    ): Response<GeocodingData?>
}