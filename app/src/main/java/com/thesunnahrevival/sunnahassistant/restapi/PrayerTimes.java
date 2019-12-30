package com.thesunnahrevival.sunnahassistant.restapi;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrayerTimes {

    private List<Datum> data;

    public PrayerTimes(List<Datum> data) {
        this.data = data;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public class Datum {

        private Timings timings;
        private Date date;

        public Datum(Timings timings, Date date) {
            this.timings = timings;
            this.date = date;
        }

        public Timings getTimings() {
            return timings;
        }

        public Date getDate() {
            return date;
        }

    }

    public class Timings {

        @SerializedName("Fajr")
        @Expose
        private String fajr;

        @SerializedName("Dhuhr")
        @Expose
        private String dhuhr;

        @SerializedName("Asr")
        @Expose
        private String asr;

        @SerializedName("Maghrib")
        @Expose
        private String maghrib;

        @SerializedName("Isha")
        @Expose
        private String isha;


        public Timings(String fajr, String dhuhr, String asr, String maghrib, String isha) {
            this.fajr = fajr;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
        }

        public String getFajr() {
            return fajr;
        }

        public String getDhuhr() {
            return dhuhr;
        }

        public String getAsr() {
            return asr;
        }

        public String getMaghrib() {
            return maghrib;
        }

        public String getIsha() {
            return isha;
        }

    }

    public class Date {

        private Gregorian gregorian;

        public Date(Gregorian gregorian) {
            this.gregorian = gregorian;
        }

        public Gregorian getGregorian() {
            return gregorian;
        }

    }

    public class Gregorian {

        private String day;

        public Gregorian(String day) {
            this.day = day;
        }

        public int getDay() {
            return Integer.parseInt(day);
        }

        public void setDay(String day) {
            this.day = day;
        }
    }

}


