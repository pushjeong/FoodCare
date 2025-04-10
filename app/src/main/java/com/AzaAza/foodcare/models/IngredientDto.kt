// IngredientDto.kt
package com.AzaAza.foodcare.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class IngredientDto(
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("purchase_date") val purchaseDate: String
)

data class IngredientResponse(
    @SerializedName("message") val message: String
)




