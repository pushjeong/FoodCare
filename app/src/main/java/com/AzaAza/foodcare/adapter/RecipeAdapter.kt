package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.Recipe
import androidx.appcompat.app.AlertDialog

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val userIngredients: List<String>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.recipeName)
        val descriptionText: TextView = itemView.findViewById(R.id.recipeDescription)
        val imageView: ImageView = itemView.findViewById(R.id.recipeImage)
        val matchedCountText: TextView = itemView.findViewById(R.id.matchedCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.nameText.text = recipe.name
        holder.descriptionText.text = recipe.description
        holder.imageView.setImageResource(recipe.imageResId)
        holder.matchedCountText.text = if (recipe.matchedIngredients.isNotEmpty())
            "일치 재료: ${recipe.matchedCount}개 (${recipe.matchedIngredients.joinToString(", ")})"
        else
            "일치하는 재료 없음"

        holder.descriptionText.setOnClickListener {
            val context = holder.itemView.context

            // 전체 조리 순서를 메시지로 구성
            val message = """
📝 레시피 설명:
${recipe.instructions}

🧂 필요한 재료:
${recipe.ingredients.joinToString(", ")}

⏱ 소요 시간: ${recipe.timeTaken ?: "알 수 없음"}
💪 난이도: ${recipe.difficulty ?: "알 수 없음"}
🩺 알레르기: ${recipe.allergies ?: "없음"}
🚫 질병 관련: ${recipe.disease ?: "없음"}
""".trimIndent()

            // 상세정보용 TextView 설정
            val textView = TextView(context).apply {
                text = message
                textSize = 16f
                setPadding(40, 40, 40, 40)
                isVerticalScrollBarEnabled = true
                movementMethod = android.text.method.ScrollingMovementMethod.getInstance()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // ScrollView에 감싸고 wrap_content로 설정하여 내용에 따라 높이 조절
            val scrollView = ScrollView(context).apply {
                addView(textView)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // 다이얼로그 생성 및 표시
            AlertDialog.Builder(context)
                .setTitle("${recipe.name} 상세 정보")
                .setView(scrollView)
                .setPositiveButton("닫기", null)
                .show()
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateList(newList: List<Recipe>) {
        recipes = newList
        notifyDataSetChanged()
    }

    fun sortRecipesByMatchedIngredients() {
        recipes = recipes.sortedByDescending { it.matchedCount }
        notifyDataSetChanged()
    }
}
