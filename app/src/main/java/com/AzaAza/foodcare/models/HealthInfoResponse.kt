package com.AzaAza.foodcare.models

data class HealthInfoResponse(
    val id: Int,
    val birth_date: String,
    val gender: String,
    val height_cm: Double,
    val weight_kg: Double,
    val food_preference: String,
    val allergens: List<Allergen>,
    val diseases: List<Disease>,
    val url: String
)