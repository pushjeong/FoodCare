package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.data.ShoppingList
import com.AzaAza.foodcare.data.ShoppingList.items
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import android.widget.Toast


class ShoppingListAdapter(
    private var data: List<Pair<String, Int>> = ShoppingList.items.toList()
) : RecyclerView.Adapter<ShoppingListAdapter.VH>() {

    // ...

    inner class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_list, parent, false)
    ) {
        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvItemCount)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

        fun bind(pair: Pair<String, Int>) {
            tvName.text = pair.first
            tvCount.text = "구매 갯수: ${pair.second}개"

            ivDelete.setOnClickListener {
                val name = pair.first
                val remaining = ShoppingList.decreaseItem(name)
                // 남은 개수에 따라 토스트 메시지
                if (remaining != null) {
                    Toast.makeText(
                        itemView.context,
                        "$name 구매 갯수 $remaining 개로 감소",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        itemView.context,
                        "$name 항목이 목록에서 제거되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // 리스트 갱신
                updateData(ShoppingList.items.toList())
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(data[position])

    override fun getItemCount() = data.size

    /** 외부에서 호출하거나 내부 삭제 후 데이터 갱신 */
    fun updateData(newData: List<Pair<String, Int>>) {
        data = newData
        notifyDataSetChanged()
    }
}