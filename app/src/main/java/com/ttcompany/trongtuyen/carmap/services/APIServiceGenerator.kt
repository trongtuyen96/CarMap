package com.ttcompany.trongtuyen.carmap.services

import com.ttcompany.trongtuyen.carmap.controllers.AppController
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIServiceGenerator {

    private val BASE_URL = "https://carmap.herokuapp.com/api-user/v1/" //must have back slash at the end

//    private val BASE_URL = "https://carmap-test.herokuapp.com/api-user/v1/" //must have back slash at the end
    private val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

    private var retrofit = builder.build()

    private val logging = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val httpClient = OkHttpClient.Builder()

    private val authentication = Interceptor { chain ->
        val original = chain.request()

        val request = original.newBuilder()
                .header("x-access-token", AppController.accessToken)
//                .header("Content-Encoding", "gzip")
                .method(original.method(), original.body())
                .build()
        chain.proceed(request)
    }

    fun retrofit(): Retrofit {
        return retrofit
    }

    fun <S> createService(serviceClass: Class<S>): S {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging)
            httpClient.addInterceptor(authentication)
            builder.client(httpClient.build())
            retrofit = builder.build()
        }

        return retrofit.create(serviceClass)
    }
}// No need to instantiate this class.