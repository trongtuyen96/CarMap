package com.example.trongtuyen.carmap.services

import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.example.trongtuyen.carmap.services.models.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

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
    @PUT("user/updateCurrentLocation")
    fun updateCurrentLocation(@Body user: User): Call<UserProfileResponse>

    @PUT("user/updateSocketID")
    fun updateSocketID(@Body user: User): Call<UserProfileResponse>

    @GET("user/nearby")
    fun getNearbyUsers(@Query("lat") lat: Double?, @Query("lng") lng: Double?, @Query("radius") radius: Float): Call<List<User>>

    @PUT("user/updateHomeLocation")
    fun updateHomeLocation(@Body user: User): Call<UserProfileResponse>

    @PUT("user/updateWorkLocation")
    fun updateWorkLocation(@Body user: User): Call<UserProfileResponse>
}