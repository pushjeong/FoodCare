package com.AzaAza.foodcare.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // const가 아닌 일반 변수로 변경
    private var baseUrl = "http://127.0.0.1:8000/recipes/"  // 안드로이드 에뮬레이터에서 로컬 서버 접속용 주소

    // Retrofit 인스턴스를 저장할 변수 추가
    private var retrofitInstance: Retrofit? = null

    // 서버 URL 변경 메서드
    fun setBaseUrl(url: String) {
        baseUrl = url
        // 인스턴스 초기화
        retrofitInstance = null
    }

    fun getBaseUrl(): String {
        return baseUrl
    }

    // lazy 대신 get() 프로퍼티 사용
    val instance: Retrofit
        get() {
            if (retrofitInstance == null) {
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofitInstance!!
        }

    val ingredientApiService: IngredientApiService
        get() = instance.create(IngredientApiService::class.java)

    val recipeApiService: RecipeApiService
        get() = instance.create(RecipeApiService::class.java)
}