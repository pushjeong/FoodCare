package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.ExpenseDto
import com.AzaAza.foodcare.models.ExpenseGroup
import java.text.NumberFormat
import java.util.*

class ExpenseGroupAdapter(
    private val expenseGroups: MutableList<ExpenseGroup>,
    private val onItemLongClick: (ExpenseDto) -> Unit
) : RecyclerView.Adapter<ExpenseGroupAdapter.ItemViewHolder>() {

    // 총계 변경 리스너
    private var onTotalChangedListener: ((Float) -> Unit)? = null

    // 총계 변경 리스너 설정
    fun setOnTotalChangedListener(listener: (Float) -> Unit) {
        this.onTotalChangedListener = listener
    }

    override fun getItemCount(): Int {
        // 모든 그룹의 확장된 항목들만 계산
        return expenseGroups.sumOf { group ->
            if (group.isExpanded) group.expenses.size else 0
        }
    }

    // 실제 위치 계산 (그룹과 아이템 인덱스)
    private fun getExpenseAtPosition(position: Int): ExpenseDto? {
        var currentPosition = 0
        for (group in expenseGroups) {
            if (group.isExpanded) {
                val groupSize = group.expenses.size
                if (position < currentPosition + groupSize) {
                    return group.expenses[position - currentPosition]
                }
                currentPosition += groupSize
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_group_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val expense = getExpenseAtPosition(position)
        if (expense != null) {
            holder.bind(expense)
            holder.itemView.setOnLongClickListener {
                onItemLongClick(expense)
                true
            }

            // 배경 설정 (모든 항목을 일반 배경으로)
            holder.itemView.setBackgroundResource(R.drawable.expense_item_normal_bg)

            // 항목 너비 조정
            val layoutParams = holder.itemView.layoutParams
            layoutParams.width = LayoutParams.MATCH_PARENT

            // 마진 제거
            if (layoutParams is MarginLayoutParams) {
                layoutParams.setMargins(0, 0, 0, 0)
            }

            holder.itemView.layoutParams = layoutParams
        }
    }

    // 아이템 삭제
    fun removeExpense(expenseId: Int): Boolean {
        for (groupIndex in expenseGroups.indices) {
            val group = expenseGroups[groupIndex]
            val itemIndex = group.expenses.indexOfFirst { it.id == expenseId }

            if (itemIndex != -1) {
                // 항목 삭제
                group.expenses.removeAt(itemIndex)

                // 그룹이 비어있으면 그룹도 제거
                if (group.expenses.isEmpty()) {
                    expenseGroups.removeAt(groupIndex)
                    notifyDataSetChanged() // 전체 갱신
                } else {
                    // 개별 항목만 제거
                    notifyDataSetChanged() // 간단하게 전체 갱신
                }

                // 총액 업데이트
                updateTotalAmount()
                return true
            }
        }
        return false
    }

    // 전체 총액 계산 및 리스너 호출
    private fun updateTotalAmount() {
        val total = expenseGroups.sumOf { it.getTotalAmount().toDouble() }.toFloat()
        onTotalChangedListener?.invoke(total)
    }

    // 아이템 뷰홀더
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.itemNameText)
        private val dateText: TextView = itemView.findViewById(R.id.itemDateText)
        private val amountText: TextView = itemView.findViewById(R.id.itemAmountText)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)

        fun bind(expense: ExpenseDto) {
            nameText.text = expense.productName

            // 날짜 부분만 추출해서 표시 ("yyyy-MM-dd HH:mm"에서 "yyyy-MM-dd" 추출)
            val dateOnly = expense.dateTime.split(" ")[0]
            dateText.text = dateOnly

            // 금액 포맷팅
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            amountText.text = "${formatter.format(expense.amount.toInt())}원"

            // 카테고리 세로줄은 모두 동일한 초록색 사용
            categoryIndicator.setBackgroundColor(android.graphics.Color.parseColor("#00E676"))
        }
    }
}