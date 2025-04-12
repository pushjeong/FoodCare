package com.AzaAza.foodcare.models

import com.AzaAza.foodcare.R
import com.google.gson.annotations.SerializedName

data class RecipeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("ingredients") val ingredients: String,  // DB에는 텍스트로 저장되어 있음
    @SerializedName("instructions") val instructions: String,
    @SerializedName("timetaken") val timetaken: String,
    @SerializedName("difficultylevel") val difficultylevel: String,
    @SerializedName("allergies") val allergies: String?,
    @SerializedName("disease") val disease: String?,
    @SerializedName("diseasereason") val diseasereason: String?,
    @SerializedName("category") val category: String?
) {
    // RecipeDto를 Recipe 객체로 변환하는 함수
    fun toRecipe(userIngredients: List<String>): Recipe {
        val ingredientsList = ingredients.split(",").map { it.trim() }
        val matched = ingredientsList.filter { it in userIngredients }

        // 음식 이름에 따라 이미지 선택
        val imageRes = when (name) {
            "김치찌개" -> R.drawable.kimchistew
            //"김치볶음밥" -> R.drawable.eggroll
            // "된장찌개" -> R.drawable.soybeanstew

            else -> R.drawable.bell  // 기본 이미지
        }

        return Recipe(
            name = name,
            description = instructions.take(50) + if (instructions.length > 50) "..." else "",
            imageResId = imageRes,
            ingredients = ingredientsList,
            matchedCount = matched.size,
            matchedIngredients = matched,
            timeTaken = timetaken,
            difficulty = difficultylevel,
            allergies = allergies,
            disease = disease,
            diseaseReason = diseasereason,
            category = category
        )
    }


}