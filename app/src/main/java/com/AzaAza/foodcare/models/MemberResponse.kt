package com.AzaAza.foodcare.models


data class MemberResponse(
    val id: Int,
    val username: String,
    val login_id: String,
    val email: String,
    val status: String,    // "pending" 또는 "accepted"
    val is_owner: Boolean,
    val profile_image_url: String?
)
