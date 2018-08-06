package com.example.trongtuyen.carmap.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Report {
    @SerializedName("_id")
    @Expose
    var _id: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("subtype1")
    @Expose
    var subtype1: String? = null
    @SerializedName("subtype2")
    @Expose
    var subtype2: String? = null
    @SerializedName("description")
    @Expose
    var description: String? = null
    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null
    @SerializedName("userID")
    @Expose
    var userID: String? = null
    @SerializedName("numReport")
    @Expose
    var numReport: Number? = null
    @SerializedName("numDelete")
    @Expose
    var numDelete: Number? = null
    @SerializedName("status")
    @Expose
    var status: Boolean? = null
    @SerializedName("byteAudioFile")
    @Expose
    var byteAudioFile: String? = null
    @SerializedName("byteImageFile")
    @Expose
    var byteImageFile: String? = null
    @SerializedName("phoneNumber")
    @Expose
    var phoneNumber: String? = null

    var distance: Double? = null

    constructor(type: String, subtype1: String, subtype2: String, description: String, geometry: Geometry, userID: String, numReport: Number, numDelete: Number, status: Boolean, byteAudioFile: String, byteImageFile: String, phoneNumber: String) {
        this.type = type
        this.subtype1 = subtype1
        this.subtype2 = subtype2
        this.description = description
        this.geometry = geometry
        this.userID = userID
        this.numReport = numReport
        this.numDelete = numDelete
        this.status = status
        this.byteAudioFile = byteAudioFile
        this.byteImageFile = byteImageFile
        this.phoneNumber = phoneNumber
    }
}