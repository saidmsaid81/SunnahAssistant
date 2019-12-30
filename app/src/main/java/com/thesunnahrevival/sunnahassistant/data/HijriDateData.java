package com.thesunnahrevival.sunnahassistant.data;

import java.util.List;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

public class HijriDateData {

    private List<Datum> data;

    public HijriDateData(List<Datum> data) {
        this.data = data;
    }

    public List<Datum> getData() {
        return data;
    }

    @Entity(tableName = "hijri_calendar")
    public static class Hijri {

        @PrimaryKey(autoGenerate = true)
        private int id;
        private String monthName;
        private String day;
        @Ignore
        private Month month;

        private String year;

        public Hijri(String day, Month month, String year) {
            this.day = day;
            this.month = month;
            this.year = year;
        }


        public Hijri(String day, String monthName, String year) {
            this.day = day;
            this.monthName = monthName;
            this.year = year;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMonthName() {
            return monthName;
        }

        public String getDay() {
            return day;
        }

        public String getYear() {
            return year;
        }

        public Month getMonth() {
            return month;
        }

    }

    public static class Month {
        private String en;

        public Month(String en) {
            this.en = en;
        }

        public String getEn() {
            return en;
        }
    }

    public class Datum {

        private Hijri hijri;

        public Datum(Hijri hijri) {
            this.hijri = hijri;
        }

        public Hijri getHijri() {
            return hijri;
        }

    }
}
