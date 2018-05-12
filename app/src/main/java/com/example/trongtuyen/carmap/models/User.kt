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
    @SerializedName("homeLocation")
    @Expose
    var homeLocation: Geometry? = null

    constructor(email: String, name: String, avatar: String, googleUserID: String, birthDate: String, createdAt: String, socketID: String, homeLocation: Geometry){
        this.email = email
        this.name = name
        this.avatar = avatar
        this.googleUserID = googleUserID
        this.birthDate = birthDate
        this.createdAt = createdAt
        this.socketID = socketID
        this.homeLocation = homeLocation
    }
}