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
    private val onClick: (CategoryDto) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryNameText: TextView = view.findViewById(R.id.categoryNameText)
        val categoryAmountText: TextView = view.findViewById(R.id.categoryAmountText)
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

        // 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onClick(category)
        }
    }

    override fun getItemCount() = categories.size
}