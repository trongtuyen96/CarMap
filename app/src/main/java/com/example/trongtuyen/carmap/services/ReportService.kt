package com.example.trongtuyen.carmap.services

import com.example.trongtuyen.carmap.models.Report
import com.example.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.example.trongtuyen.carmap.services.models.ReportResponse
import retrofit2.Call
import retrofit2.http.*

interface ReportService {

    @get:GET("report/{id}")
    val report: Call<ReportResponse>

    @get:GET("report")
    val allReport: Call<List<Report>>

    // @FormUrlEncoded
    // Bỏ FormUrlEncoded vì gây lỗi với @Body
    @PUT("report/{id}/updateNumReport")
    fun updateNumReport(@Path("id") reportID: String, @Body report: Report): Call<ReportResponse>

    @PUT("report/{id}/updateNumDelete")
    fun updateNumDelete(@Path("id") reportID: String, @Body report: Report): Call<ReportResponse>

    @POST("report")
    fun addNewReport(@Body report: Report): Call<Report>

//    @GET("report/nearby")
//    fun getNearbyReports(@Query("lat") lat: Double?, @Query("lng") lng: Double?, @Query("radius") radius: Float): Call<List<Report>>

    @GET("report/nearby")
    fun getNearbyReports(@Query("lat") lat: Double?, @Query("lng") lng: Double?, @Query("radius") radius: Float): Call<NearbyReportsResponse>

    @PUT("report/{id}/updateBase64Voice")
    fun updateBase64Voice(@Path("id") id: String, @Field("base64Voice") base64Voice: String): Call<ReportResponse>

    @DELETE("report/{id}/delete")
    fun deleteReport(@Path("id") id: String): Call<ReportResponse>

}