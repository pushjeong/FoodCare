package com.AzaAza.foodcare.models

// 날짜별 그룹화된 지출 내역
data class ExpenseGroup(
    val date: String,         // 날짜 (yyyy-MM-dd)
    val displayTitle: String, // 표시 제목 ("오늘", "어제", 또는 날짜)
    val expenses: MutableList<ExpenseDto> = mutableListOf(),
    var isExpanded: Boolean = false
) {
    // 그룹의 총 금액 계산
    fun getTotalAmount(): Double {
        return expenses.sumOf { it.amount }
    }
}