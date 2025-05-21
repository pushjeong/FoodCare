package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_LAST_ITEM = 2  // 그룹의 마지막 항목 (하단 라운드 처리)
    }

    // 총계 변경 리스너
    private var onTotalChangedListener: ((Float) -> Unit)? = null

    // 총계 변경 리스너 설정
    fun setOnTotalChangedListener(listener: (Float) -> Unit) {
        this.onTotalChangedListener = listener
    }

    // 뷰 타입 결정
    override fun getItemViewType(position: Int): Int {
        var itemCount = 0
        for (group in expenseGroups) {
            // 헤더인 경우
            if (position == itemCount) {
                return TYPE_HEADER
            }

            itemCount++

            // 확장된 그룹인 경우 아이템도 포함
            if (group.isExpanded) {
                val groupItemCount = group.expenses.size
                if (position < itemCount + groupItemCount) {
                    // 그룹의 마지막 아이템인지 확인
                    val itemPosition = position - itemCount
                    if (itemPosition == groupItemCount - 1) {
                        return TYPE_LAST_ITEM  // 그룹의 마지막 항목
                    }
                    return TYPE_ITEM
                }
                itemCount += groupItemCount
            }
        }
        return TYPE_HEADER // 기본값
    }

    // 실제 위치 계산 (그룹과 아이템 인덱스)
    private fun getPositionData(position: Int): Pair<Int, Int> {
        var itemCount = 0
        for (i in expenseGroups.indices) {
            // 헤더인 경우
            if (position == itemCount) {
                return Pair(i, -1)
            }

            itemCount++

            // 확장된 그룹인 경우 아이템도 포함
            if (expenseGroups[i].isExpanded) {
                val groupItemCount = expenseGroups[i].expenses.size
                if (position < itemCount + groupItemCount) {
                    return Pair(i, position - itemCount)
                }
                itemCount += groupItemCount
            }
        }
        return Pair(0, -1) // 기본값
    }

    override fun getItemCount(): Int {
        var count = 0
        for (group in expenseGroups) {
            count++ // 헤더
            if (group.isExpanded) {
                count += group.expenses.size // 아이템
            }
        }
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.expense_group_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_ITEM, TYPE_LAST_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.expense_group_item, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val posData = getPositionData(position)
        val groupPosition = posData.first
        val itemPosition = posData.second

        when (holder) {
            is HeaderViewHolder -> {
                val group = expenseGroups[groupPosition]
                holder.bind(group)

                // 헤더 배경 모양 설정 (펼쳐진 상태인지 아닌지에 따라 다름)
                val headerContainer = holder.itemView.findViewById<LinearLayout>(R.id.headerContainer)
                if (group.isExpanded) {
                    // 펼쳐진 상태 - 상단만 둥글게
                    headerContainer.setBackgroundResource(R.drawable.expense_group_header_top_rounded)
                } else {
                    // 접힌 상태 - 모든 모서리 둥글게
                    headerContainer.setBackgroundResource(R.drawable.expense_group_header_all_rounded)
                }

                // 헤더 너비 조정 - 마진 적용 (날짜 그룹 사이 간격 추가)
                val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
                layoutParams.width = LayoutParams.MATCH_PARENT

                // 그룹 간 마진 추가 - 첫 번째 항목이 아닌 경우에만 상단 마진 추가
                if (groupPosition > 0) {
                    layoutParams.topMargin = 24  // 24dp의 상단 마진 추가
                } else {
                    layoutParams.topMargin = 0  // 첫 번째 그룹은 상단 마진 없음
                }

                holder.itemView.layoutParams = layoutParams
            }
            is ItemViewHolder -> {
                val expense = expenseGroups[groupPosition].expenses[itemPosition]
                holder.bind(expense)
                holder.itemView.setOnLongClickListener {
                    onItemLongClick(expense)
                    true
                }

                // 뷰 타입에 따라 배경 설정
                val viewType = getItemViewType(position)
                if (viewType == TYPE_LAST_ITEM) {
                    // 그룹의 마지막 항목 - 하단만 둥글게
                    holder.itemView.setBackgroundResource(R.drawable.expense_item_bottom_rounded_bg)
                } else {
                    // 중간 항목 - 직각 처리
                    holder.itemView.setBackgroundResource(R.drawable.expense_item_normal_bg)
                }

                // 항목 너비 조정 - 헤더와 동일하게
                val layoutParams = holder.itemView.layoutParams
                layoutParams.width = LayoutParams.MATCH_PARENT

                // 마진 제거 (이미 레이아웃에서 패딩으로 처리)
                if (layoutParams is MarginLayoutParams) {
                    layoutParams.setMargins(0, 0, 0, 0)
                }

                holder.itemView.layoutParams = layoutParams
            }
        }
    }

    // 아이템 삭제 (완전히 개선된 버전)
    fun removeExpense(expenseId: Int): Boolean {
        for (groupIndex in expenseGroups.indices) {
            val group = expenseGroups[groupIndex]
            val itemIndex = group.expenses.indexOfFirst { it.id == expenseId }

            if (itemIndex != -1) {
                // 항목이 확장된 그룹에 있는지 확인 (축소된 그룹은 보이지 않음)
                if (!group.isExpanded) {
                    // 그룹이 축소된 상태에서는 데이터만 제거하고 UI는 갱신하지 않음
                    group.expenses.removeAt(itemIndex)
                    if (group.expenses.isEmpty()) {
                        expenseGroups.removeAt(groupIndex)
                    }

                    // 총액 업데이트
                    updateTotalAmount()
                    return true
                }

                // 절대 어댑터 위치 계산
                var adapterPosition = 0

                // 현재 그룹 이전의 모든 그룹에 대한 위치 계산
                for (i in 0 until groupIndex) {
                    adapterPosition++ // 각 그룹의 헤더
                    if (expenseGroups[i].isExpanded) {
                        adapterPosition += expenseGroups[i].expenses.size // 확장된 그룹의 항목들
                    }
                }

                adapterPosition++ // 현재 그룹의 헤더
                adapterPosition += itemIndex // 현재 그룹 내 항목의 인덱스

                // 항목 삭제
                group.expenses.removeAt(itemIndex)

                // 그룹이 비어있으면 그룹도 제거
                if (group.expenses.isEmpty()) {
                    expenseGroups.removeAt(groupIndex)
                    notifyDataSetChanged() // 전체 갱신

                    // 총액 업데이트
                    updateTotalAmount()
                    return true
                }

                // 항목 제거 알림
                notifyItemRemoved(adapterPosition)

                // 마지막 항목이 삭제된 경우, 새로운 마지막 항목의 배경을 업데이트
                if (itemIndex == group.expenses.size) {
                    notifyItemChanged(adapterPosition - 1)
                }

                // 삭제된 항목 이후의 모든 항목 위치 업데이트
                if (itemIndex < group.expenses.size) {
                    notifyItemRangeChanged(adapterPosition, group.expenses.size - itemIndex)
                }

                // 헤더 총액 업데이트
                notifyItemChanged(adapterPosition - itemIndex - 1)

                // 총액 업데이트
                updateTotalAmount()

                return true
            }
        }

        return false // 항목을 찾지 못함
    }

    // 전체 총액 계산 및 리스너 호출
    private fun updateTotalAmount() {
        val total = expenseGroups.sumOf { it.getTotalAmount().toDouble() }.toFloat()
        onTotalChangedListener?.invoke(total)
    }

    // 헤더(그룹) 뷰홀더
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerContainer: LinearLayout = itemView.findViewById(R.id.headerContainer)
        private val titleText: TextView = itemView.findViewById(R.id.groupTitleText)
        private val totalText: TextView = itemView.findViewById(R.id.groupTotalText)
        private val expandIcon: ImageView = itemView.findViewById(R.id.groupExpandIcon)

        fun bind(group: ExpenseGroup) {
            titleText.text = group.displayTitle

            // 금액 포맷팅
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            totalText.text = "총 ${formatter.format(group.getTotalAmount().toInt())}원"

            // 확장 아이콘 상태 설정
            if (group.isExpanded) {
                expandIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                expandIcon.setImageResource(R.drawable.ic_arrow_down)
            }

            // 클릭 리스너
            headerContainer.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val posData = getPositionData(position)
                    val groupPosition = posData.first

                    // 그룹 확장/축소 토글
                    val group = expenseGroups[groupPosition]
                    group.isExpanded = !group.isExpanded

                    // 헤더 업데이트 (아이콘 및 배경 변경)
                    notifyItemChanged(position)

                    // 아이템 추가/제거
                    if (group.isExpanded) {
                        // 확장: 아이템 추가됨을 알림
                        notifyItemRangeInserted(position + 1, group.expenses.size)
                    } else {
                        // 축소: 아이템 제거됨을 알림
                        notifyItemRangeRemoved(position + 1, group.expenses.size)
                    }
                }
            }
        }
    }

    // 아이템 뷰홀더
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.itemNameText)
        private val amountText: TextView = itemView.findViewById(R.id.itemAmountText)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)

        fun bind(expense: ExpenseDto) {
            nameText.text = expense.productName

            // 금액 포맷팅
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            amountText.text = "${formatter.format(expense.amount.toInt())}원"

            // 카테고리 세로줄은 모두 동일한 초록색 사용 (이미지에 맞춤)
            categoryIndicator.setBackgroundColor(android.graphics.Color.parseColor("#00E676"))
        }
    }
}