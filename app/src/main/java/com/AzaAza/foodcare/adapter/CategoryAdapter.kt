package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.CategoryDto
import java.text.NumberFormat
import java.util.*

class CategoryAdapter(
    private val categories: List<CategoryDto>,
    private val onCategoryClick: (CategoryDto) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categoryColors: List<Int> = listOf()

    // 색상 정보 업데이트 메서드
    fun updateColors(colors: List<Int>) {
        categoryColors = colors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameText: TextView = itemView.findViewById(R.id.categoryNameText)
        private val categoryAmountText: TextView = itemView.findViewById(R.id.categoryAmountText)
        private val categoryColorIndicator: View = itemView.findViewById(R.id.categoryColorIndicator)

        fun bind(category: CategoryDto, position: Int) {
            categoryNameText.text = category.name

            // 금액 포맷팅
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            categoryAmountText.text = "${formatter.format(category.totalAmount.toInt())}원"

            // 색상 인디케이터 설정 (사각형 배경 + tintColor)
            if (position < categoryColors.size) {
                val color = categoryColors[position]
                categoryColorIndicator.setBackgroundResource(R.drawable.category_color_square)
                categoryColorIndicator.background.setTint(color)
            } else {
                categoryColorIndicator.setBackgroundResource(R.drawable.category_color_square)
                categoryColorIndicator.background.setTint(0xFFAAAAAA.toInt()) // 기본 회색
            }

            // 클릭 이벤트
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}
