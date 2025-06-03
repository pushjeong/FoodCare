package com.AzaAza.foodcare.api

import com.AzaAza.foodcare.models.CategoryDto
import com.AzaAza.foodcare.models.ExpenseDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ExpenseApiService {
    @GET("expense_categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("expenses")
    suspend fun getExpenses(): List<ExpenseDto>

    @POST("expenses")
    suspend fun addExpense(@Body expense: ExpenseDto): Response<ExpenseDto>

    @DELETE("expenses/{expense_id}")
    suspend fun deleteExpense(@Path("expense_id") expenseId: Int): Response<Void>

    /*  미사용으로 삭제 됨
    @GET("expenses/category/{category_id}")
    suspend fun getExpensesByCategory(@Path("category_id") categoryId: Int): List<ExpenseDto>
    */
    @GET("expenses/summary/monthly")
    suspend fun getMonthlySummary(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): MonthlySummaryResponse
}

data class MonthlySummaryResponse(
    @SerializedName("year") val year: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("categories") val categories: List<CategorySummary>
)

data class CategorySummary(
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("percentage") val percentage: Double
)