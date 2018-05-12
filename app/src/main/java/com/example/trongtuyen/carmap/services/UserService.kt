package com.example.trongtuyen.carmap.services

import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/**
 * Created by tuyen on 08/05/2018.
 */

interface UserService {
    @get:GET("user/profile")
    val userProfile: Call<UserProfileResponse>

    @get:GET("user")
    val allUserProfile: Call<List<User>>

    // @FormUrlEncoded
    // Bỏ FormUrlEncoded vì gây lỗi với @Body
    @PUT("user/updateHomeLocation")
    abstract fun updateHomeLocation(@Body user: User): Call<UserProfileResponse>
}