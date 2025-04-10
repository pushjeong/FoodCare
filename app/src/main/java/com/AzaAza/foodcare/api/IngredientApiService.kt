package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.models.IngredientResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IngredientApiService {
    @POST("/ingredients")
    fun addIngredient(@Body ingredient: IngredientDto): Call<IngredientResponse>

    @GET("/ingredients")
    fun getIngredients(): Call<List<IngredientDto>>
}