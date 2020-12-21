package com.thesunnahrevival.sunnahassistant.data.remote

import retrofit2.http.POST
import retrofit2.http.Query

interface SunnahAssistantApiInterface {

    @POST("reportGeocodingError")
    suspend fun reportGeocodingError(
            @Query("status") status: String
    )
}