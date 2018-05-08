package com.example.trongtuyen.carmap.services

import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by tuyen on 08/05/2018.
 */

interface UserService {
    @get:GET("user/profile")
    val userProfile: Call<UserProfileResponse>
}