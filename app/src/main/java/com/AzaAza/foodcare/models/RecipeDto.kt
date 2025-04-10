package com.AzaAza.foodcare.models

import com.AzaAza.foodcare.Recipe
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
        // 쉼표로 구분된 문자열을 리스트로 변환
        val ingredientsList = ingredients.split(",").map { it.trim() }

        // 일치하는 재료 수 계산
        val matchedCount = ingredientsList.count { it in userIngredients }

        return Recipe(
            name = name,
            description = instructions.take(50) + if (instructions.length > 50) "..." else "",
            imageResId = com.AzaAza.foodcare.R.drawable.bell,
            ingredients = ingredientsList,
            matchedCount = matchedCount
        )
    }
}