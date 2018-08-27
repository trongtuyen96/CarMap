package com.ttcompany.trongtuyen.carmap.services

import com.ttcompany.trongtuyen.carmap.models.Report
import com.ttcompany.trongtuyen.carmap.services.models.NearbyReportsResponse
import com.ttcompany.trongtuyen.carmap.services.models.ReportResponse
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
    fun updateNumReport(@Path("id") reportID: String): Call<ReportResponse>

    @PUT("report/{id}/updateNumDelete")
    fun updateNumDelete(@Path("id") reportID: String): Call<ReportResponse>

    @POST("report")
    fun addNewReport(@Body report: Report): Call<Report>

//    @GET("report/nearby")
//    fun getNearbyReports(@Query("lat") lat: Double?, @Query("lng") lng: Double?, @Query("radius") radius: Float): Call<List<Report>>

    @GET("report/nearby")
    fun getNearbyReports(@Query("lat") lat: Double?, @Query("lng") lng: Double?, @Query("radius") radius: Float): Call<NearbyReportsResponse>

    @FormUrlEncoded
    @PUT("report/{id}/updateBase64Voice")
    fun updateBase64Voice(@Path("id") id: String, @Field("base64Voice") base64Voice: String): Call<ReportResponse>

    @DELETE("report/{id}/delete")
    fun deleteReport(@Path("id") id: String): Call<ReportResponse>

    //    @DELETE("report/{id}/delete")
//    fun deleteReport(@Path("id") id: String): Call<SampleResponse>

    @GET("report/{id}/getBase64Image")
    fun getBase64ImageReport(@Path("id") id: String): Call<ReportResponse>

    @GET("report/{id}/getBase64Voice")
    fun getBase64VoiceReport(@Path("id") id: String): Call<ReportResponse>
}