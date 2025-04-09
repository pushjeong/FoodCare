package com.AzaAza.foodcare

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener

class FoodRecipeActivity : AppCompatActivity() {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeList: List<Recipe>
    private val userIngredients = listOf("밥", "김", "계란", "파", "두부")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_recipe)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // 레시피 샘플 데이터 + 보유재료 일치 수 계산
        recipeList = listOf(
            Recipe("김밥", "간단한 김밥", R.drawable.bibimbap, listOf("밥", "김", "당근", "계란")),
            Recipe("계란말이", "부드러운 계란요리", R.drawable.kimchistew, listOf("계란", "소금", "파")),
            Recipe("된장찌개", "전통 한식 찌개", R.drawable.shabushabu, listOf("된장", "두부", "호박", "파", "마늘"))
        )

        // 보유재료 일치 개수 계산 및 정렬
        val sortedList = recipeList.map { recipe ->
            val matched = recipe.ingredients.count { it in userIngredients }
            recipe.copy(matchedCount = matched)
        }.sortedByDescending { it.matchedCount }

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.recipeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeAdapter(sortedList)
        recyclerView.adapter = recipeAdapter

        // 검색 필터 적용
        val searchBar: EditText = findViewById(R.id.search_bar)
        searchBar.addTextChangedListener {
            val filtered = RecipeSearchHelper.filter(it.toString(), recipeList)
                .map { recipe ->
                    val matched = recipe.ingredients.count { it in userIngredients }
                    recipe.copy(matchedCount = matched)
                }.sortedByDescending { it.matchedCount }

            recipeAdapter.updateList(filtered)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
