package com.thesunnahrevival.sunnahassistant.data.model.dto

data class GeocodingData(val results: List<Result>, val status: String)

data class Result(val formattedAddress: String, val geometry: Geometry)

data class Geometry(val location: Location)

data class Location(val lat: Float, val lng: Float)