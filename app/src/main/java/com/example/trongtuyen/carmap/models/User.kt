package com.example.trongtuyen.carmap.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by tuyen on 07/05/2018.
 */

class User {
    @SerializedName("_id")
    @Expose
    var _id: String? = null
    @SerializedName("email")
    @Expose
    var email: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("avatar")
    @Expose
    var avatar: String? = null
    @SerializedName("googleUserID")
    @Expose
    var googleUserID: String? = null
    @SerializedName("birthDate")
    @Expose
    var birthDate: String? = null
    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null
    @SerializedName("socketID")
    @Expose
    var socketID: String? = null
    @SerializedName("currentLocation")
    @Expose
    var currentLocation: Geometry? = null
    @SerializedName("latHomeLocation")
    @Expose
    var latHomeLocation: Double? = null
    @SerializedName("longHomeLocation")
    @Expose
    var longHomeLocation: Double? = null
    @SerializedName("latWorkLocation")
    @Expose
    var latWorkLocation: Double? = null
    @SerializedName("longWorkLocation")
    @Expose
    var longWorkLocation: Double? = null
    @SerializedName("typeCar")
    @Expose
    var typeCar: String? = null
    @SerializedName("modelCar")
    @Expose
    var modelCar: String? = null
    @SerializedName("colorCar")
    @Expose
    var colorCar: String? = null


    constructor(email: String, name: String, avatar: String, googleUserID: String, birthDate: String, createdAt: String, socketID: String, currentLocation: Geometry, latHomeLocation: Double, longHomeLocation: Double, latWorkLocation: Double, longWorkLocation: Double, typeCar: String, modelCar: String, colorCar: String) {
        this.email = email
        this.name = name
        this.avatar = avatar
        this.googleUserID = googleUserID
        this.birthDate = birthDate
        this.createdAt = createdAt
        this.socketID = socketID
        this.currentLocation = currentLocation
        this.latHomeLocation = latHomeLocation
        this.longHomeLocation = longHomeLocation
        this.latWorkLocation = latWorkLocation
        this.longWorkLocation = longWorkLocation
        this.typeCar = typeCar
        this.modelCar = modelCar
        this.colorCar = colorCar
    }
}