package com.AzaAza.foodcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.Recipe

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
        holder.matchedCountText.text =
            "일치 재료: ${recipe.matchedCount}개 (${recipe.ingredients.filter { userIngredients.contains(it) }.joinToString(", ")})"
    }

    override fun getItemCount(): Int = recipes.size

    fun updateList(newList: List<Recipe>) {
        recipes = newList
        notifyDataSetChanged()
    }
}