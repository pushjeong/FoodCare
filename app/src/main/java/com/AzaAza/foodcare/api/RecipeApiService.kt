package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.RecipeDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RecipeApiService {
    @GET("/recipes")
    fun getRecipes(): Call<List<RecipeDto>>

    @GET("/recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<RecipeDto>
}