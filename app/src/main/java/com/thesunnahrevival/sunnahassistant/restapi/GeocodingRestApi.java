package com.thesunnahrevival.sunnahassistant.restapi;

import com.thesunnahrevival.sunnahassistant.ApiKey;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeocodingRestApi {

    private static GeocodingRestApi instance = null;
    private final GeocodingInterface mGeocodingInterface;
    private MutableLiveData<GeocodingData> mData = new MutableLiveData<>();

    private GeocodingRestApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mGeocodingInterface = retrofit.create(GeocodingInterface.class);
    }

    public static GeocodingRestApi getInstance() {
        if (instance == null)
            instance = new GeocodingRestApi();
        return instance;
    }

    public void fetchGeocodingData(String address) {
        mGeocodingInterface.getGeocodingData(address, ApiKey.API_KEY).enqueue(new Callback<GeocodingData>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingData> call, @NonNull Response<GeocodingData> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                mData.setValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingData> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public MutableLiveData<GeocodingData> getData() {
        return mData;
    }
}
