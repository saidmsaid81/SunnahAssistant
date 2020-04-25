package com.thesunnahrevival.sunnahassistant.data.remote;

import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData;
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AladhanInterface {

    @GET("gToHCalendar/{monthNumber}/{year}")
    Call<HijriDateData> getHijriCalendar(
            @Path("monthNumber") String monthNumber,
            @Path("year") String year,
            @Query("adjustment") int adjustment
    );

    @GET("/calendar")
    Call<PrayerTimes> getPrayerTimes(
            @Query("latitude") float latitude,
            @Query("longitude") float longitude,
            @Query("month") String month,
            @Query("year") String year,
            @Query("method") int method,
            @Query("school") int asrCalculationMethod,
            @Query("latitudeAdjustmentMethod") int adjustmentMethod
    );


}
