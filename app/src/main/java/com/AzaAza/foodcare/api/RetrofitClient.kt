package com.AzaAza.foodcare.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"  // 안드로이드 에뮬레이터에서 로컬 서버 접속용 주소

    // BASE_URL 가져오는 메서드 추가
    fun getBaseUrl(): String {
        return BASE_URL
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val ingredientApiService: IngredientApiService by lazy {
        instance.create(IngredientApiService::class.java)
    }

    val recipeApiService: RecipeApiService by lazy {
        instance.create(RecipeApiService::class.java)
    }
}