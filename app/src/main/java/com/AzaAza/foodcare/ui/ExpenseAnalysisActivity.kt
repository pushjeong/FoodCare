package com.AzaAza.foodcare.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ExpenseAnalysisActivity : AppCompatActivity() {

    private val allTopIngredients = mutableListOf<Pair<String, Int>>()  // 전체 10개 저장용
    private val shoppingListItems = mutableListOf<Triple<String, Int, Int>>()  // ("이름", 구매횟수, 수량)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_analysis)

        val topLayout = findViewById<LinearLayout>(R.id.topIngredientsLayout)
        val recommendLayout = findViewById<LinearLayout>(R.id.recommendLayout)
        val moreButton = findViewById<Button>(R.id.btnMore)
        val checkListButton = findViewById<Button>(R.id.btnCheckList)
        val inflater = LayoutInflater.from(this)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        loadSavedShoppingList()  // 🔹 SharedPreferences 에서 불러오기
        loadTopIngredients(inflater, topLayout)
        recommendRecipesWithTopIngredients(recommendLayout)

        moreButton.setOnClickListener {
            val dialog = MoreIngredientsDialogFragment()
            dialog.show(supportFragmentManager, "MoreIngredientsDialog")
        }

        checkListButton.setOnClickListener {
            val dialog = ShoppingListDialogFragment(shoppingListItems) {
                saveShoppingList() // 🔹 저장 콜백
            }
            dialog.show(supportFragmentManager, "ShoppingListDialog")
        }
    }

    private fun loadTopIngredients(inflater: LayoutInflater, topLayout: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expenses = RetrofitClient.expenseApiService.getExpenses()
                val groceryExpenses = expenses.filter { it.categoryName == "장보기" }

                val top10 = groceryExpenses
                    .groupingBy { it.productName }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(10)

                allTopIngredients.clear()
                allTopIngredients.addAll(top10)

                withContext(Dispatchers.Main) {
                    topLayout.removeAllViews()

                    allTopIngredients.take(5).forEachIndexed { index, (name, count) ->
                        val cardView = inflater.inflate(R.layout.item_ingredient_card, topLayout, false)
                        cardView.findViewById<TextView>(R.id.rankCircle).text = (index + 1).toString()
                        cardView.findViewById<TextView>(R.id.ingredientName).text = name
                        cardView.findViewById<TextView>(R.id.frequencyText).text = "월 ${count}회 구매"

                        cardView.findViewById<Button>(R.id.addButton).setOnClickListener {
                            if (shoppingListItems.none { it.first == name }) {
                                shoppingListItems.add(Triple(name, count, 1))
                                saveShoppingList()
                                Toast.makeText(this@ExpenseAnalysisActivity, "$name 을(를) 쇼핑리스트에 추가했습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ExpenseAnalysisActivity, "$name 은(는) 이미 추가되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        topLayout.addView(cardView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* 미사용으로 삭제 됨
    private fun showMoreIngredients(inflater: LayoutInflater, topLayout: LinearLayout) {
        val startIndex = 5
        val moreIngredients = allTopIngredients.drop(startIndex)

        if (topLayout.childCount > 5) return

        moreIngredients.forEachIndexed { i, (name, count) ->
            val index = i + startIndex
            val cardView = inflater.inflate(R.layout.item_ingredient_card, topLayout, false)
            cardView.findViewById<TextView>(R.id.rankCircle).text = (index + 1).toString()
            cardView.findViewById<TextView>(R.id.ingredientName).text = name
            cardView.findViewById<TextView>(R.id.frequencyText).text = "월 ${count}회 구매"

            cardView.findViewById<Button>(R.id.addButton).setOnClickListener {
                if (shoppingListItems.none { it.first == name }) {
                    shoppingListItems.add(Triple(name, count, 1))
                    saveShoppingList()
                    Toast.makeText(this, "$name 을(를) 쇼핑리스트에 추가했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$name 은(는) 이미 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            topLayout.addView(cardView)
        }
    }
*/
    private fun recommendRecipesWithTopIngredients(recommendLayout: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expenses = RetrofitClient.expenseApiService.getExpenses()
                val groceryExpenses = expenses.filter { it.categoryName == "장보기" }

                val topIngredients = groceryExpenses
                    .map { it.productName }
                    .distinct()
                    .take(10)

                val recipeResponse = RetrofitClient.recipeApiService.getRecipes().execute()
                if (!recipeResponse.isSuccessful || recipeResponse.body() == null) return@launch

                val matchedRecipes = recipeResponse.body()!!
                    .map { it.toRecipe(topIngredients) }
                    .filter { it.matchedCount > 0 }
                    .sortedByDescending { it.matchedCount }
                    .take(5)

                withContext(Dispatchers.Main) {
                    recommendLayout.removeAllViews()
                    val topCount = topIngredients.size

                    matchedRecipes.forEach { recipe ->
                        val textView = TextView(this@ExpenseAnalysisActivity).apply {
                            text = "${recipe.name}\n상위 ${topCount}개 중 ${recipe.matchedCount}개 포함"
                            textSize = 14f
                            setPadding(24, 28, 24, 28)
                            background = getDrawable(R.drawable.recommend_item_bg)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 12, 0, 12)
                            }
                        }
                        recommendLayout.addView(textView)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 🔸 SharedPreferences 저장 (name, count, quantity 포함)
    private fun saveShoppingList() {
        val prefs = getSharedPreferences("shopping_list", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        shoppingListItems.forEach { (name, count, quantity) ->
            val obj = JSONObject()
            obj.put("name", name)
            obj.put("count", count)
            obj.put("quantity", quantity)
            jsonArray.put(obj)
        }
        prefs.edit().putString("items", jsonArray.toString()).apply()
    }

    // 🔸 SharedPreferences 불러오기
    private fun loadSavedShoppingList() {
        val prefs = getSharedPreferences("shopping_list", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("items", null) ?: return

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val count = obj.getInt("count")
                val quantity = obj.optInt("quantity", 1)
                shoppingListItems.add(Triple(name, count, quantity))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
