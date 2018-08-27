package com.ttcompany.trongtuyen.carmap.models.nearbyplaces

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Models

class NearbyPlacesResponse(val results: List<NearbyPlacesResultsResponse>)

class NearbyPlacesResultsResponse(
        val name: String,
        val geometry: NearbyPlacesGeometryResponse
)

class NearbyPlacesGeometryResponse(val location: NearbyPlacesLocationResponse)

class NearbyPlacesLocationResponse(
        val lat: Double,
        val lng: Double
)

// HTTP to Interface using Retrofit

interface NearbyPlacesInterface{
    @GET("api/place/nearbysearch/json?key=AIzaSyDTWxpGP0Zjgifxrau0BrNdzebFmuUKEpI")
//    @GET("api/place/nearbysearch/json?key=AIzaSyA5ZhjuU748p6U0B9bCRC0ojgfdwJo5fgA")

    fun getNearbyPlaces(@Query("type") type: String,
                        @Query("location") location: String,
                        @Query("radius") radius: Int
                        ): Call<NearbyPlacesResponse>
}