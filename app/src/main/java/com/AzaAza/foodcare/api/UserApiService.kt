package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.UserRequest
import com.AzaAza.foodcare.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {
    @POST("/user")
    fun signUp(@Body request: UserRequest): Call<UserResponse>

    @POST("/login")   // 새로 추가하는 로그인
    fun login(@Body req: UserRequest): Call<UserResponse>
}