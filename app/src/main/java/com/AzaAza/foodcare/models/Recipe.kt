package com.AzaAza.foodcare.models

data class Recipe(
    val name: String,
    val description: String,
    val imageResId: Int,  // 이미지 리소스 ID
    val ingredients: List<String>,
    val matchedCount: Int = 0,
    val matchedIngredients: List<String> = emptyList(),
    val timeTaken: String? = null,
    val difficulty: String? = null,
    val allergies: String? = null,
    val disease: String? = null,
    val diseaseReason: String? = null,
    val category: String? = null
)