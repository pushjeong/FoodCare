package com.AzaAza.foodcare

data class Recipe(
    val name: String,
    val description: String,
    val imageResId: Int,
    val ingredients: List<String>,
    val matchedCount: Int = 0 // 보유재료 일치 개수 필드 추가
)


