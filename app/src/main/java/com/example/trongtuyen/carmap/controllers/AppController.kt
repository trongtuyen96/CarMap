package com.example.trongtuyen.carmap.controllers

import android.graphics.Bitmap
import android.location.Location
import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.utils.SharePrefs
import com.google.android.gms.location.places.Place

/**
 * Created by tuyen on 07/05/2018.
 */

object AppController {
    // Save current user info - include token
//    lateinit var userLocation: Location

    var userProfile: User? = null

    internal var USER_ACCESS_TOKEN = "user_access_token"
    internal var SETTINGS_FILTER_CAR = "setting_filter_car"
    internal var SETTINGS_FILTER_REPORT = "setting_filter_report"
    internal var SETTINGS_SOUND_MODE = "setting_sound_mode"
    internal var SETTINGS_INVISIBLE = "setting_invisible"
    internal var SETTINGS_SOCKET = "setting_socket"
    internal var SETTINGS_USER_RADIUS = "setting_user_radius"
    internal var SETTINGS_REPORT_RADIUS = "setting_report_radius"

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

//    // Danh sách tài xế
//    lateinit var listUser: List<User>
//
//    // Danh sách các báo cáo
//    lateinit var listReport: List<Report>

//    // fileName của thu âm lúc report
//    lateinit var fileAudioName : String

    // Bitmap của ảnh Report tài xế khác
    var bitmapReportOther: Bitmap? = null

    // Base64 của ảnh report tài xế khác
    var base64ImageReportOther: String = ""
    var typeReportOther: String = ""
    var licensePlate: String = ""

    var settingFilterCar: String?
        get() = SharePrefs.instance?.GetString(SETTINGS_FILTER_CAR)
        set(value) {
            SharePrefs.instance?.SetString(SETTINGS_FILTER_CAR, value!!)
        }

    var settingFilterReport: String?
        get() = SharePrefs.instance?.GetString(SETTINGS_FILTER_REPORT)
        set(value) {
            SharePrefs.instance?.SetString(SETTINGS_FILTER_REPORT, value!!)
        }

    // Chế độ âm thanh
    var soundMode: Int?
        get() = SharePrefs.instance?.GetInt(SETTINGS_SOUND_MODE)
        set(value) {
            SharePrefs.instance?.SetInt(SETTINGS_SOUND_MODE, value!!)
        }

    var settingInvisible: String?
        get() = SharePrefs.instance?.GetString(SETTINGS_INVISIBLE)
        set(value) {
            SharePrefs.instance?.SetString(SETTINGS_INVISIBLE, value!!)
        }

    var settingSocket: String?
        get() = SharePrefs.instance?.GetString(SETTINGS_SOCKET)
        set(value) {
            SharePrefs.instance?.SetString(SETTINGS_SOCKET, value!!)
        }

    var settingReportRadius: Int?
        get() = SharePrefs.instance?.GetInt(SETTINGS_REPORT_RADIUS)
        set(value) {
            SharePrefs.instance?.SetInt(SETTINGS_REPORT_RADIUS, value!!)
        }

    var settingUserRadius: Int?
        get() = SharePrefs.instance?.GetInt(SETTINGS_USER_RADIUS)
        set(value) {
            SharePrefs.instance?.SetInt(SETTINGS_USER_RADIUS, value!!)
        }

    // Lịch sử tìm kiếm gần đây
    var listHistoryPlace: MutableList<Place> = ArrayList()

}
