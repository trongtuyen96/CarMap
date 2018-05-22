package com.example.trongtuyen.carmap.controllers

import android.location.Location
import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.utils.SharePrefs

/**
 * Created by tuyen on 07/05/2018.
 */

object AppController {
    // Save current user info - include token
    lateinit var userLocation: Location

    var userProfile: User? = null

    internal var USER_ACCESS_TOKEN = "user_access_token"

    var accessToken: String?
        get() = SharePrefs.instance?.GetString(USER_ACCESS_TOKEN)
        set(accessToken) {
            SharePrefs.instance?.SetString(USER_ACCESS_TOKEN, accessToken!!)
        }

    val isSignedIn: Boolean
        get() = userProfile != null && accessToken != null

    fun signOut() {
        this.userProfile = null
        this.accessToken = ""
    }

    // Danh sách tài xế
    lateinit var listUser: List<User>

    // Danh sách các báo cáo
    lateinit var listReport: List<Report>



}
