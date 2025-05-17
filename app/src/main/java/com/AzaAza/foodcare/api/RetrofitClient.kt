package com.AzaAza.foodcare.api

import UserApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://foodcare-69ae76eec1bf.herokuapp.com/"
    private const val TIMEOUT_SECONDS = 30L

    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    val ingredientApiService: IngredientApiService by lazy {
        instance.create(IngredientApiService::class.java)
    }

    val recipeApiService: RecipeApiService by lazy {
        instance.create(RecipeApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        instance.create(UserApiService::class.java)
    }

    val expenseApiService: ExpenseApiService by lazy {
        instance.create(ExpenseApiService::class.java)
    }
}