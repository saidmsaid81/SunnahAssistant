package com.thesunnahrevival.sunnahassistant.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodingData {

    private List<Result> results;

    public GeocodingData(List<Result> results) {
        this.results = results;
    }

    public List<Result> getResults() {
        return results;
    }

    public class Result {

        @SerializedName("formatted_address")
        @Expose
        private String formattedAddress;

        private Geometry geometry;

        public Result(String formattedAddress, Geometry geometry) {
            this.formattedAddress = formattedAddress;
            this.geometry = geometry;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public Geometry getGeometry() {
            return geometry;
        }

    }

    public class Geometry {

        private Location location;

        public Geometry(Location location) {
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

    }

    public class Location {

        private Float lat;
        private Float lng;

        public Location(Float lat, Float lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Float getLat() {
            return lat;
        }

        public Float getLng() {
            return lng;
        }

    }

}
