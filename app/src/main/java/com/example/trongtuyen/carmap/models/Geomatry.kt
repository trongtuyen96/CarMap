package com.example.trongtuyen.carmap.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Geometry(type: String, coordinates: List<Double>) {

    @SerializedName("coordinates")
    @Expose
    var coordinates: List<Double>? = coordinates

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = type

}