package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.IngredientDto
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ExpiryNotificationActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val displayDateFormat = SimpleDateFormat("MM월 dd일", Locale.KOREA)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class Ingredient(
        val id: Int,
        val name: String,
        val location: String,
        val expiryDate: Date,
        val purchaseDate: Date,
        val imageUrl: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expiry_notification)

        container = findViewById(R.id.expiryNotificationContainer)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // 서버에서 식자재 데이터 불러오기
        fetchIngredientsFromServer()
    }

    private fun fetchIngredientsFromServer() {
        RetrofitClient.ingredientApiService.getIngredients().enqueue(object : Callback<List<IngredientDto>> {
            override fun onResponse(call: Call<List<IngredientDto>>, response: Response<List<IngredientDto>>) {
                if (response.isSuccessful) {
                    val ingredients = response.body()
                    if (ingredients != null) {
                        processIngredients(ingredients)
                    } else {
                        showToast("데이터를 불러오는데 실패했습니다: 빈 응답")
                    }
                } else {
                    showToast("데이터를 불러오는데 실패했습니다: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<IngredientDto>>, t: Throwable) {
                showToast("서버 연결에 실패했습니다: ${t.message}")
                Log.e("ExpiryNotification", "서버 연결 실패", t)
            }
        })
    }

    private fun processIngredients(ingredientDtos: List<IngredientDto>) {
        val ingredients = mutableListOf<Ingredient>()

        // DTO를 Ingredient 객체로 변환
        for (dto in ingredientDtos) {
            try {
                val expiryDate = apiDateFormat.parse(dto.expiryDate) ?: continue
                val purchaseDate = apiDateFormat.parse(dto.purchaseDate) ?: continue

                ingredients.add(
                    Ingredient(
                        id = dto.id,
                        name = dto.name,
                        location = dto.location,
                        expiryDate = expiryDate,
                        purchaseDate = purchaseDate,
                        imageUrl = dto.imageUrl
                    )
                )
            } catch (e: Exception) {
                Log.e("ExpiryNotification", "날짜 파싱 오류", e)
            }
        }

        // 오늘 날짜 가져오기
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // 표시할 식자재 목록
        // 1. 소비기한이 3일 이내인 식자재
        // 2. 소비기한이 지난 식자재
        val nearExpiryAndExpiredIngredients = ingredients.filter { ingredient ->
            // 소비기한이 지난 경우
            val isExpired = ingredient.expiryDate.before(today)

            // 소비기한이 3일 이내인 경우
            val cal = Calendar.getInstance().apply { time = today }
            cal.add(Calendar.DAY_OF_MONTH, 3) // 오늘로부터 3일 후
            val threeDaysLater = cal.time
            val isNearExpiry = ingredient.expiryDate.after(today) &&
                    !ingredient.expiryDate.after(threeDaysLater)

            // 두 조건 중 하나라도 만족하면 표시
            isExpired || isNearExpiry
        }

        if (nearExpiryAndExpiredIngredients.isEmpty()) {
            // 소비기한이 임박하거나 지난 식자재가 없는 경우
            val emptyView = TextView(this).apply {
                text = "소비기한이 임박하거나 지난 식자재가 없습니다."
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 0)
            }
            container.addView(emptyView)
        } else {
            // 소비기한이 임박하거나 지난 식자재 표시 (소비기한 순으로 정렬)
            nearExpiryAndExpiredIngredients
                .sortedBy { it.expiryDate }
                .forEach { ingredient ->
                    addIngredientCard(ingredient)
                }
        }
    }

    private fun addIngredientCard(ingredient: Ingredient) {
        // 카드 뷰 생성
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16.dpToPx(this@ExpiryNotificationActivity))
            }
            radius = 12.dpToPx(this@ExpiryNotificationActivity).toFloat()
            cardElevation = 2.dpToPx(this@ExpiryNotificationActivity).toFloat()
        }

        // 카드 내용 레이아웃
        val cardContent = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dpToPx(this@ExpiryNotificationActivity),
                    16.dpToPx(this@ExpiryNotificationActivity),
                    16.dpToPx(this@ExpiryNotificationActivity),
                    16.dpToPx(this@ExpiryNotificationActivity))
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // 식재료 아이콘 이미지뷰
        val iconImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                70.dpToPx(this@ExpiryNotificationActivity),
                70.dpToPx(this@ExpiryNotificationActivity)
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = 16.dpToPx(this@ExpiryNotificationActivity)
            }

            // 이미지 설정
            if (ingredient.imageUrl != null) {
                Glide.with(this@ExpiryNotificationActivity)
                    .load("https://foodcare-69ae76eec1bf.herokuapp.com${ingredient.imageUrl}")
                    .into(this)
            } else {
                setImageResource(R.drawable.basicfood) // 기본 이미지
            }
        }

        // 텍스트 정보를 담을 레이아웃
        val textContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // 식재료 이름 텍스트뷰
        val nameTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = ingredient.name
            textSize = 20f
            setTextColor(Color.BLACK)
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }

        // 소비기한 텍스트뷰 (라운드 배경)
        val expiryTextView = TextView(this).apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dpToPx(this@ExpiryNotificationActivity)
            }
            layoutParams = params

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diffDays = ((ingredient.expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

            // 소비기한 상태에 따른 메시지 설정
            text = if (diffDays < 0) {
                val daysOverdue = Math.abs(diffDays)
                "소비기한이 ${daysOverdue}일 지났습니다! ${ingredient.name}이(가) 포함된 레시피를 추천해 드릴까요?"
            } else if (diffDays == 0) {
                "소비기한이 오늘까지입니다! ${ingredient.name}이(가) 포함된 레시피를 추천해 드릴까요?"
            } else {
                "소비기한이 ${diffDays}일 남았습니다! ${ingredient.name}이(가) 포함된 레시피를 추천해 드릴까요?"
            }

            // 색상 설정
            when {
                diffDays < 0 -> {
                    setBackgroundResource(R.drawable.expiry_background) // 빨간색 배경
                    setTextColor(Color.WHITE)
                }
                diffDays <= 3 -> {
                    setBackgroundResource(R.drawable.expiry_warning_background) // 주황색 배경
                    setTextColor(Color.WHITE)
                }
                else -> {
                    setBackgroundResource(0)
                    setTextColor(Color.GRAY)
                }
            }

            setPadding(
                16.dpToPx(this@ExpiryNotificationActivity),
                8.dpToPx(this@ExpiryNotificationActivity),
                16.dpToPx(this@ExpiryNotificationActivity),
                8.dpToPx(this@ExpiryNotificationActivity)
            )
        }

        // 위젯 추가
        textContainer.addView(nameTextView)
        textContainer.addView(expiryTextView)

        cardContent.addView(iconImageView)
        cardContent.addView(textContainer)

        cardView.addView(cardContent)

        // 카드 클릭 이벤트 추가 - 식자재를 기반으로 레시피 검색
        cardView.setOnClickListener {
            // 선택한 식자재 이름으로 레시피 검색 화면으로 이동
            val intent = Intent(this, RecipeSearchActivity::class.java)
            intent.putExtra("SELECTED_INGREDIENT", ingredient.name)
            startActivity(intent)
        }

        container.addView(cardView)
    }

    // 확장 함수 - dp를 px로 변환
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}