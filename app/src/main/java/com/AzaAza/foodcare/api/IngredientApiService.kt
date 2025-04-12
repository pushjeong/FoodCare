package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.models.IngredientResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// API 인터페이스 정의
interface IngredientApiService {
    @Multipart
    @POST("/ingredients")
    fun addIngredient(
        @Part("name") name: RequestBody,
        @Part("location") location: RequestBody,
        @Part("expiry_date") expiryDate: RequestBody,
        @Part("purchase_date") purchaseDate: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<IngredientResponse>

    @GET("/ingredients")
    fun getIngredients(): Call<List<IngredientDto>>

    @DELETE("/ingredients/{id}")
    fun deleteIngredient(@Path("id") id: Int): Call<IngredientResponse>
}