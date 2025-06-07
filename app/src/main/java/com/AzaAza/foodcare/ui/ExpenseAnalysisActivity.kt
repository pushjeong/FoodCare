package com.AzaAza.foodcare.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.data.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ExpenseAnalysisActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ExpenseAnalysisActivity"
        private const val PREFS_PERSONAL_SHOPPING_LIST = "personal_shopping_list"
        private const val PREFS_SHARED_SHOPPING_LIST = "shared_shopping_list"
    }

    private val allTopIngredients = mutableListOf<Pair<String, Int>>()
    private val shoppingListItems = mutableListOf<Triple<String, Int, Int>>()

    private var isSharedMode: Boolean = false
    private var groupOwnerId: Int = -1
    private var currentUserId: Int = -1

    // 모드 전환 UI
    private lateinit var personalModeTab: LinearLayout
    private lateinit var sharedModeTab: LinearLayout
    private lateinit var personalModeIcon: ImageView
    private lateinit var sharedModeIcon: ImageView
    private lateinit var personalModeText: TextView
    private lateinit var sharedModeText: TextView
    private lateinit var sharedModeInfo: LinearLayout
    private lateinit var sharedModeInfoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_analysis)

        // 사용자 정보 초기화
        currentUserId = UserSession.getUserId(this)
        if (currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 모드 UI 초기화
        personalModeTab = findViewById(R.id.personalModeTab)
        sharedModeTab = findViewById(R.id.sharedModeTab)
        personalModeIcon = findViewById(R.id.personalModeIcon)
        sharedModeIcon = findViewById(R.id.sharedModeIcon)
        personalModeText = findViewById(R.id.personalModeText)
        sharedModeText = findViewById(R.id.sharedModeText)
        sharedModeInfo = findViewById(R.id.sharedModeInfo)
        sharedModeInfoText = findViewById(R.id.sharedModeInfoText)

        personalModeTab.setOnClickListener {
            if (isSharedMode) {
                isSharedMode = false
                groupOwnerId = currentUserId
                updateModeUI()
                reloadData()
            }
        }

        sharedModeTab.setOnClickListener {
            if (!isSharedMode) {
                isSharedMode = true
                updateModeUI()
                reloadData()
            }
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener { onBackPressed() }

        // 모드 설정 초기화
        initializeModeSettings()
        updateModeUI()
        updateTitle()

        findViewById<Button>(R.id.btnMore).setOnClickListener {
            val dialog = if (isSharedMode) {
                MoreIngredientsDialogFragment.newInstance(groupOwnerId, true)
            } else {
                MoreIngredientsDialogFragment.newInstance(currentUserId, false)
            }
            dialog.show(supportFragmentManager, "MoreIngredientsDialog")
        }

        findViewById<Button>(R.id.btnCheckList).setOnClickListener {
            val dialog = ShoppingListDialogFragment(shoppingListItems) {
                saveShoppingList()
            }
            dialog.show(supportFragmentManager, "ShoppingListDialog")
        }

        reloadData()
    }

    private fun initializeModeSettings() {
        val intentSharedMode = intent.getBooleanExtra("shared_mode", false)
        val intentOwnerId = intent.getIntExtra("group_owner_id", -1)

        isSharedMode = intentSharedMode && intentOwnerId != -1
        groupOwnerId = if (isSharedMode) intentOwnerId else currentUserId

        Log.d(TAG, "모드 설정: ${if (isSharedMode) "공유" else "개인"}, groupOwnerId: $groupOwnerId")
    }

    private fun updateModeUI() {
        if (isSharedMode) {
            personalModeTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            personalModeIcon.setColorFilter(android.graphics.Color.parseColor("#999999"))
            personalModeText.setTextColor(android.graphics.Color.parseColor("#999999"))

            // 공유 모드 활성화 - 초록색 테마로 변경
            sharedModeTab.setBackgroundResource(R.drawable.bg_toggle_selected_green_rect)
            sharedModeIcon.setColorFilter(android.graphics.Color.parseColor("#00C896"))  // 초록색
            sharedModeText.setTextColor(android.graphics.Color.parseColor("#00C896"))    // 초록색

            sharedModeInfo.visibility = View.VISIBLE
            // 가족 구성원 정보 가져오기
            updateSharedModeInfo()
        } else {
            sharedModeTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            sharedModeIcon.setColorFilter(android.graphics.Color.parseColor("#999999"))
            sharedModeText.setTextColor(android.graphics.Color.parseColor("#999999"))

            // 개인 모드 활성화 - 기존 파란색 유지
            personalModeTab.setBackgroundResource(R.drawable.bg_toggle_selected_blue_rect)
            personalModeIcon.setColorFilter(android.graphics.Color.parseColor("#007AFF"))
            personalModeText.setTextColor(android.graphics.Color.parseColor("#007AFF"))

            sharedModeInfo.visibility = View.GONE
        }
    }

    private fun updateTitle() {
        val titleText = findViewById<TextView>(R.id.topBarTitle)
        titleText?.text = "소비패턴 분석" // 항상 동일한 제목 사용
    }

    private fun reloadData() {
        loadSavedShoppingList()
        val inflater = LayoutInflater.from(this)
        loadTopIngredients(inflater, findViewById(R.id.topIngredientsLayout))
        recommendRecipesWithTopIngredients(findViewById(R.id.recommendLayout))
        updateTitle()
    }

    fun addToShoppingList(name: String, count: Int) {
        if (shoppingListItems.none { it.first == name }) {
            shoppingListItems.add(Triple(name, count, 1))
            saveShoppingList()
            val message = if (isSharedMode) {
                "$name 을(를) 가족 쇼핑리스트에 추가했습니다."
            } else {
                "$name 을(를) 쇼핑리스트에 추가했습니다."
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "$name 은(는) 이미 추가되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTopIngredients(inflater: LayoutInflater, topLayout: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val top10 = if (isSharedMode) {
                    val sharedExpenses = RetrofitClient.expenseApiService.getSharedExpenses(groupOwnerId)
                    val groceryExpenses = sharedExpenses.filter { expense ->
                        expense.categoryName?.trim()?.equals("장보기", true) == true
                    }
                    groceryExpenses.groupingBy { expense -> expense.productName.trim() }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .take(10)
                } else {
                    val personalExpenses = RetrofitClient.expenseApiService.getExpenses(currentUserId)
                    val groceryExpenses = personalExpenses.filter { expense ->
                        expense.categoryName?.trim()?.equals("장보기", true) == true
                    }
                    groceryExpenses.groupingBy { expense -> expense.productName.trim() }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .take(10)
                }

                allTopIngredients.clear()
                allTopIngredients.addAll(top10)

                withContext(Dispatchers.Main) {
                    topLayout.removeAllViews()
                    if (allTopIngredients.isEmpty()) {
                        val noDataText = TextView(this@ExpenseAnalysisActivity).apply {
                            text = if (isSharedMode) "가족의 장보기 내역이 없습니다." else "장보기 내역이 없습니다."
                            textSize = 14f
                            setPadding(24, 28, 24, 28)
                            gravity = android.view.Gravity.CENTER
                        }
                        topLayout.addView(noDataText)
                    } else {
                        allTopIngredients.take(5).forEachIndexed { index, (name, count) ->
                            val cardView = inflater.inflate(R.layout.item_ingredient_card, topLayout, false)
                            cardView.findViewById<TextView>(R.id.rankCircle).text = (index + 1).toString()
                            cardView.findViewById<TextView>(R.id.ingredientName).text = name
                            cardView.findViewById<TextView>(R.id.frequencyText).text = "월 ${count}회 구매"

                            cardView.findViewById<Button>(R.id.addButton).setOnClickListener {
                                addToShoppingList(name, count)
                            }
                            topLayout.addView(cardView)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "상위 재료 로드 실패", e)
            }
        }
    }

    private fun recommendRecipesWithTopIngredients(recommendLayout: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val topIngredients = if (isSharedMode) {
                    val sharedExpenses = RetrofitClient.expenseApiService.getSharedExpenses(groupOwnerId)
                    sharedExpenses.filter { expense ->
                        expense.categoryName?.trim()?.equals("장보기", true) == true
                    }
                        .map { expense -> expense.productName.trim() }
                        .distinct()
                        .take(10)
                } else {
                    val personalExpenses = RetrofitClient.expenseApiService.getExpenses(currentUserId)
                    personalExpenses.filter { expense ->
                        expense.categoryName?.trim()?.equals("장보기", true) == true
                    }
                        .map { expense -> expense.productName.trim() }
                        .distinct()
                        .take(10)
                }

                val recipeResponse = RetrofitClient.recipeApiService.getRecipes().execute()
                if (!recipeResponse.isSuccessful || recipeResponse.body() == null) return@launch

                val matchedRecipes = recipeResponse.body()!!
                    .map { it.toRecipe(topIngredients) }
                    .filter { it.matchedCount > 0 }
                    .sortedByDescending { it.matchedCount }
                    .take(5)

                withContext(Dispatchers.Main) {
                    recommendLayout.removeAllViews()
                    if (matchedRecipes.isEmpty()) {
                        val noData = TextView(this@ExpenseAnalysisActivity).apply {
                            text = "추천 레시피가 없습니다."
                            setPadding(24, 28, 24, 28)
                        }
                        recommendLayout.addView(noData)
                    } else {
                        matchedRecipes.forEach { recipe ->
                            val textView = TextView(this@ExpenseAnalysisActivity).apply {
                                text = "${recipe.name}\n상위 재료 ${recipe.matchedCount}개 포함"
                                setPadding(24, 28, 24, 28)
                                background = getDrawable(R.drawable.recommend_item_bg)
                            }
                            recommendLayout.addView(textView)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "레시피 추천 실패", e)
            }
        }
    }

    private fun saveShoppingList() {
        val prefsName = if (isSharedMode) {
            "${PREFS_SHARED_SHOPPING_LIST}_$groupOwnerId"
        } else {
            "${PREFS_PERSONAL_SHOPPING_LIST}_$currentUserId"
        }

        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
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

    private fun loadSavedShoppingList() {
        val prefsName = if (isSharedMode) {
            "${PREFS_SHARED_SHOPPING_LIST}_$groupOwnerId"
        } else {
            "${PREFS_PERSONAL_SHOPPING_LIST}_$currentUserId"
        }

        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val jsonString = prefs.getString("items", null) ?: return

        try {
            val jsonArray = JSONArray(jsonString)
            shoppingListItems.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                shoppingListItems.add(
                    Triple(
                        obj.getString("name"),
                        obj.getInt("count"),
                        obj.optInt("quantity", 1)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "쇼핑리스트 로드 실패", e)
        }
    }

    fun getCurrentSettings(): Triple<Boolean, Int, Int> {
        return Triple(isSharedMode, groupOwnerId, currentUserId)
    }

    // 가족 구성원 정보 업데이트
    private fun updateSharedModeInfo() {
        if (!isSharedMode || groupOwnerId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val members = RetrofitClient.expenseApiService.getMembers(groupOwnerId)
                val userId = UserSession.getUserId(this@ExpenseAnalysisActivity)
                val currentUser = members.find { it.id == userId }

                // 대표자 이름 추출
                val ownerName = members.find { it.isOwner }?.username ?: "알 수 없음"
                val totalMemberCount = members.size

                withContext(Dispatchers.Main) {
                    val infoText = if (currentUser?.isOwner == true) {
                        // 대표자인 경우: 구성원 이름만 표시
                        val memberNames = members.filter { !it.isOwner }
                            .map { it.username }
                            .joinToString(", ")

                        "가족 ${totalMemberCount}명과 공유 중 · $ownerName (대표)" +
                                if (memberNames.isNotEmpty()) ", $memberNames" else ""
                    } else {
                        // 구성원인 경우: 본인 포함한 전체 구성원 이름 표시
                        val memberNames = members.filter { !it.isOwner }
                            .map { it.username }
                            .joinToString(", ")

                        "가족 ${totalMemberCount}명과 공유 중 · $ownerName (대표)" +
                                if (memberNames.isNotEmpty()) ", $memberNames" else ""
                    }

                    sharedModeInfoText.text = infoText
                    Log.d(TAG, "공유 모드 정보 로드 완료: $infoText")
                }

            } catch (exception: Exception) {
                Log.e(TAG, "공유 모드 정보 로드 실패", exception)
                withContext(Dispatchers.Main) {
                    sharedModeInfoText.text = "공유 그룹 정보 로드 실패"
                }
            }
        }
    }
}