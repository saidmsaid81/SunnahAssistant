package com.thesunnahrevival.sunnahassistant.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GeocodingData(val results: List<Result>, val status: String)

data class Result(@field:Expose @field:SerializedName("formatted_address") val formattedAddress: String, val geometry: Geometry)

data class Geometry(val location: Location)

data class Location(val lat: Float, val lng: Float)