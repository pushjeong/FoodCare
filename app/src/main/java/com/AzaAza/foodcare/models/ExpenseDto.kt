package com.AzaAza.foodcare.models

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("category_id") val id: Int,
    @SerializedName("category_name") val name: String,
    @SerializedName("category_icon") val icon: String? = null,
    var totalAmount: Double = 0.0 // 클라이언트 계산용 필드
)

data class ExpenseDto(
    @SerializedName("expense_id") val id: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("expense_date") val dateTime: String,
    @SerializedName("memo") val memo: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)