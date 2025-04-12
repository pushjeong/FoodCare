package com.AzaAza.foodcare.models

import com.google.gson.annotations.SerializedName

data class IngredientDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("purchase_date") val purchaseDate: String,
    @SerializedName("image_url") val imageUrl: String?
)
