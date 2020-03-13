package com.thesunnahrevival.sunnahassistant.data.remote;

import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingInterface {

    @GET("maps/api/geocode/json")
    Call<GeocodingData> getGeocodingData(
            @Query("address") String address,
            @Query("key") String apiKey
    );

}
