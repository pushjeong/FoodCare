package com.AzaAza.foodcare.adapter

import android.view.Gravity
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
        val matchedCountText: TextView = itemView.findViewById(R.id.matchedCountText) // 추가
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

        val matchedText = if (recipe.matchedIngredients.isNotEmpty()) {
            "일치 재료: ${recipe.matchedCount}개 (${recipe.matchedIngredients.joinToString(", ")})"
        } else {
            "일치하는 재료 없음"
        }
        holder.matchedCountText.text = matchedText

        // 설명 클릭 시 팝업 표시
        holder.descriptionText.setOnClickListener {
            val message = """
        📝 레시피 설명:
        ${recipe.description}

        🧂 필요한 재료:
        ${recipe.ingredients.joinToString(", ")}

        ⏱ 소요 시간: ${recipe.timeTaken ?: "알 수 없음"}
        💪 난이도: ${recipe.difficulty ?: "알 수 없음"}
        🩺 알레르기: ${recipe.allergies ?: "없음"}
        🚫 질병 관련: ${recipe.disease ?: "없음"}
    """.trimIndent()

            val context = holder.itemView.context

            // 텍스트 뷰
            val textView = TextView(context).apply {
                text = message
                textSize = 16f
                setPadding(40, 40, 40, 40)
                isVerticalScrollBarEnabled = true
                movementMethod = android.text.method.ScrollingMovementMethod.getInstance() // 텍스트 자체에 스크롤 허용
            }

            // 스크롤 뷰로 감싸기
            val scrollView = ScrollView(context).apply {
                addView(textView)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // AlertDialog 생성
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

    // 일치하는 재료 수에 따라 레시피를 정렬
    fun sortRecipesByMatchedIngredients() {
        recipes = recipes.sortedByDescending { it.matchedCount }
        notifyDataSetChanged()
    }
}