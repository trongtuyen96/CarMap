package com.ttcompany.trongtuyen.carmap.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Geometry(type: String, coordinates: List<Double>) {

    // vị trí 0 là Long
    // vị trí 1 là Lat
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