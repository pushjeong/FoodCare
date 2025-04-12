package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.adapter.RecipeAdapter
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.Recipe
import com.AzaAza.foodcare.models.RecipeDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeSearchActivity : AppCompatActivity() {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var allRecipes = listOf<Recipe>()

    // 사용자의 식재료 목록 - 실제로는 DB에서 가져오거나 다른 방법으로 얻어야 함
    private val userIngredients = listOf("밥", "김", "계란", "파", "두부")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_search)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // 프로그레스바 설정
        progressBar = findViewById(R.id.progressBar)

        // RecyclerView 설정
        recipeRecyclerView = findViewById(R.id.recipeRecyclerView)
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeAdapter(emptyList(), userIngredients)
        recipeRecyclerView.adapter = recipeAdapter

        // 서버에서 레시피 데이터 가져오기
        fetchRecipesFromServer()

        // 검색 필터 적용
        val searchBar: EditText = findViewById(R.id.search_bar)
        searchBar.addTextChangedListener {
            filterRecipes(it.toString())
        }
    }

    private fun fetchRecipesFromServer() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.recipeApiService.getRecipes().enqueue(object : Callback<List<RecipeDto>> {
            override fun onResponse(call: Call<List<RecipeDto>>, response: Response<List<RecipeDto>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val recipeDtos = response.body()
                    if (recipeDtos != null) {
                        // DTO를 Recipe 객체로 변환
                        allRecipes = recipeDtos.map { dto ->
                            dto.toRecipe(userIngredients)
                        }.sortedByDescending { it.matchedCount }

                        // 어댑터 업데이트
                        recipeAdapter.updateList(allRecipes)

                        Toast.makeText(this@RecipeSearchActivity,
                            "${allRecipes.size}개의 레시피를 가져왔습니다.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RecipeSearchActivity,
                            "서버에서 데이터를 불러오는데 실패했습니다: 빈 응답",
                            Toast.LENGTH_SHORT).show()

                        // 김치찌개 데이터만 보여주기
                        showKimchiStewData()
                    }
                } else {
                    Toast.makeText(this@RecipeSearchActivity,
                        "서버에서 데이터를 불러오는데 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT).show()

                    // 김치찌개 데이터만 보여주기
                    showKimchiStewData()
                }
            }

            override fun onFailure(call: Call<List<RecipeDto>>, t: Throwable) {
                progressBar.visibility = View.GONE

                Toast.makeText(this@RecipeSearchActivity,
                    "서버 연결에 실패했습니다: ${t.message}",
                    Toast.LENGTH_LONG).show()

                // 김치찌개 데이터만 보여주기
                showKimchiStewData()
            }
        })
    }

    private fun showKimchiStewData() {
        val ingredientsList = listOf("김치", "돼지고기", "두부", "대파", "양파", "마늘", "고춧가루", "국간장")
        val kimchiStew = Recipe(
            name = "김치찌개",
            description = ingredientsList.joinToString(", "),
            imageResId = R.drawable.kimchistew,
            ingredients = ingredientsList
        )


        // 보유재료 일치 개수 계산
        val matchedCount = kimchiStew.ingredients.count { it in userIngredients }
        val finalRecipe = kimchiStew.copy(matchedCount = matchedCount)

        // 어댑터 업데이트
        allRecipes = listOf(finalRecipe)
        recipeAdapter.updateList(allRecipes)

        Toast.makeText(this,
            "김치찌개 데이터를 표시합니다.",
            Toast.LENGTH_SHORT).show()
    }

    private fun filterRecipes(query: String) {
        if (query.isEmpty()) {
            recipeAdapter.updateList(allRecipes)
            return
        }

        val filtered = allRecipes.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.ingredients.any { ing -> ing.contains(query, ignoreCase = true) }
        }

        recipeAdapter.updateList(filtered)
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