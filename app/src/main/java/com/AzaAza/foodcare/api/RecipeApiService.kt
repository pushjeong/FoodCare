package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.RecipeCreateRequest
import com.AzaAza.foodcare.models.RecipeCreateResponse
import com.AzaAza.foodcare.models.RecipeDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RecipeApiService {
    // 기존 레시피 조회
    @GET("/recipes")
    fun getRecipes(): Call<List<RecipeDto>>

    @GET("/recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<RecipeDto>

    // 새로운 레시피 등록 - POST 메서드 추가
    @POST("/recipes")
    fun createRecipe(@Body request: RecipeCreateRequest): Call<RecipeCreateResponse>

}