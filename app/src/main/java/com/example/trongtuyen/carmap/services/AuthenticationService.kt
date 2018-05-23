package com.example.trongtuyen.carmap.services

import com.example.trongtuyen.carmap.models.User
import com.example.trongtuyen.carmap.services.models.AuthenticationResponse
import com.example.trongtuyen.carmap.services.models.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationService {
    @FormUrlEncoded
    @POST("auth/facebook")
    fun authWithFacebook(@Field("facebookAccessToken") facebookAccessToken: String): Call<AuthenticationResponse>

    @FormUrlEncoded
    @POST("auth/email")
    fun authWithEmail(@Field("email") email: String, @Field("password") password: String): Call<AuthenticationResponse>

    @FormUrlEncoded
    @POST("auth/refresh_token")
    fun refreshToken(@Field("accessToken") accessToken: String): Call<RefreshTokenResponse>

    @FormUrlEncoded
    @POST("register/email")
    fun registerWithEmail(@Field("email") email: String, @Field("password") password: String, @Field("name") name: String, @Field("birthDate") birthDate: String): Call<User>
}