package com.AzaAza.foodcare.models

data class UserRequest(
    val email: String,
    val password: String // 서버에 컬럼명이 passward 이므로 철자 그대로 맞춤
)
