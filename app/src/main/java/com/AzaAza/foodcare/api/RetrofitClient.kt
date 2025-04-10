// RetrofitClient.kt
package com.AzaAza.foodcare.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"  // 안드로이드 에뮬레이터에서 로컬 서버 접속용 주소

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: IngredientApiService by lazy {
        instance.create(IngredientApiService::class.java)
    }
}