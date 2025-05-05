package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.adapter.RecipeAdapter
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.helper.RecipeSearchHelper
import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.models.Recipe
import com.AzaAza.foodcare.models.RecipeDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeSearchActivity : AppCompatActivity() {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchEditText: EditText
    private lateinit var selectedIngredientLabel: TextView
    private var allRecipes = listOf<Recipe>()
    private var userIngredients = listOf<String>()
    private var selectedIngredient: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_search)

        // 선택된 식자재가 있는지 확인
        selectedIngredient = intent.getStringExtra("SELECTED_INGREDIENT")

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // 프로그레스바 설정
        progressBar = findViewById(R.id.progressBar)

        // 선택된 식자재 라벨 설정
        selectedIngredientLabel = findViewById(R.id.selectedIngredientLabel)
        if (selectedIngredient != null) {
            selectedIngredientLabel.text = "선택된 식자재: $selectedIngredient"
            selectedIngredientLabel.visibility = View.VISIBLE
        } else {
            selectedIngredientLabel.visibility = View.GONE
        }

        // 검색바 설정
        searchEditText = findViewById(R.id.searchEditText)

        // 선택된 식자재가 있으면 검색창에 표시
        if (!selectedIngredient.isNullOrEmpty()) {
            searchEditText.setText(selectedIngredient)
        }

        searchEditText.addTextChangedListener { text ->
            filterRecipes(text.toString())
        }

        // RecyclerView 설정
        recipeRecyclerView = findViewById(R.id.recipeRecyclerView)
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeAdapter(emptyList(), userIngredients)
        recipeRecyclerView.adapter = recipeAdapter

        // 서버에서 사용자 재료 목록과 레시피 데이터를 가져오기
        fetchUserIngredients()
    }

    private fun fetchUserIngredients() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.ingredientApiService.getIngredients().enqueue(object : Callback<List<IngredientDto>> {
            override fun onResponse(
                call: Call<List<IngredientDto>>,
                response: Response<List<IngredientDto>>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val ingredients = response.body()
                    if (ingredients != null) {
                        userIngredients = ingredients.map { it.name }
                        fetchRecipesFromServer()
                    } else {
                        Toast.makeText(
                            this@RecipeSearchActivity,
                            "재료 목록을 불러오는데 실패했습니다: 빈 응답",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RecipeSearchActivity,
                        "재료 목록을 불러오는데 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<IngredientDto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@RecipeSearchActivity,
                    "서버 연결에 실패했습니다: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun fetchRecipesFromServer() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.recipeApiService.getRecipes().enqueue(object : Callback<List<RecipeDto>> {
            override fun onResponse(
                call: Call<List<RecipeDto>>,
                response: Response<List<RecipeDto>>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val recipeDtos = response.body()
                    if (recipeDtos != null) {
                        // DTO를 Recipe 객체로 변환하고 일치 재료 개수로 정렬
                        allRecipes = recipeDtos.map { it.toRecipe(userIngredients) }
                            .sortedByDescending { it.matchedCount }

                        // 선택된 식자재로 초기 필터링 수행
                        if (!selectedIngredient.isNullOrEmpty()) {
                            filterRecipes(selectedIngredient!!)
                        } else {
                            recipeAdapter = RecipeAdapter(allRecipes, userIngredients)
                            recipeRecyclerView.adapter = recipeAdapter
                        }

                        Toast.makeText(
                            this@RecipeSearchActivity,
                            "${allRecipes.size}개의 레시피를 가져왔습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RecipeSearchActivity,
                            "레시피 데이터를 불러오는데 실패했습니다: 빈 응답",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RecipeSearchActivity,
                        "레시피 데이터를 불러오는데 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<RecipeDto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@RecipeSearchActivity,
                    "서버 연결에 실패했습니다: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun filterRecipes(query: String) {
        if (query.isEmpty() && selectedIngredient.isNullOrEmpty()) {
            recipeAdapter.updateList(allRecipes)
            return
        }

        val queryToUse = if (query.isNotEmpty()) query else selectedIngredient ?: ""

        // 검색어 기준으로 필터링
        val nameFiltered = RecipeSearchHelper.filter(queryToUse, allRecipes)

        // 검색어와 일치하는 식자재를 포함하는 레시피도 추가
        val ingredientFiltered = allRecipes.filter { recipe ->
            recipe.ingredients.any { ingredient ->
                ingredient.contains(queryToUse, ignoreCase = true)
            }
        }

        // 두 결과 합치기 (중복 제거)
        val filteredList = (nameFiltered + ingredientFiltered).distinct()
            .sortedByDescending { it.matchedCount }

        recipeAdapter.updateList(filteredList)

        // 선택된 식자재 표시 라벨 업데이트
        if (query.isEmpty() && !selectedIngredient.isNullOrEmpty()) {
            selectedIngredientLabel.text = "선택된 식자재: $selectedIngredient"
            selectedIngredientLabel.visibility = View.VISIBLE
        } else if (query.isNotEmpty()) {
            selectedIngredientLabel.text = "검색: $query"
            selectedIngredientLabel.visibility = View.VISIBLE
        } else {
            selectedIngredientLabel.visibility = View.GONE
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