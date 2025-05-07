package com.AzaAza.foodcare.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.CategoryDto
import java.text.NumberFormat
import java.util.*

class CategoryAdapter(
    private val categories: List<CategoryDto>,
    private val onClick: (CategoryDto) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // 카테고리 색상 저장할 리스트
    private val categoryColors = mutableListOf<Int>()

    // 색상 업데이트 메서드
    fun updateColors(colors: List<Int>) {
        categoryColors.clear()
        categoryColors.addAll(colors)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryNameText: TextView = view.findViewById(R.id.categoryNameText)
        val categoryAmountText: TextView = view.findViewById(R.id.categoryAmountText)
        val cardView: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameText.text = category.name

        // 금액 포맷팅
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        holder.categoryAmountText.text = "${formatter.format(category.totalAmount.toInt())}원"

        // 테두리 색상 설정 (색상 리스트가 비어있지 않은 경우에만)
        if (categoryColors.isNotEmpty()) {
            // 원형 차트와 동일한 색상 사용 (위치에 맞게)
            val colorIndex = position % categoryColors.size
            val color = categoryColors[colorIndex]

            // 테두리 설정을 위한 drawable 생성
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = 8 * holder.itemView.context.resources.displayMetrics.density // 8dp를 픽셀로 변환
            shape.setColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            shape.setStroke(5, color) // 5px 두께와 해당 색상으로 테두리 설정

            // 배경으로 설정 (API 16 이상 필요)
            holder.cardView.background = shape
        }

        // 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onClick(category)
        }
    }

    override fun getItemCount() = categories.size
}