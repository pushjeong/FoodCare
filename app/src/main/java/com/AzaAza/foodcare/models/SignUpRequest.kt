package com.AzaAza.foodcare.models

data class SignUpRequest(
    val login_id: String,
    val username: String,
    val email: String,
    val password: String
)