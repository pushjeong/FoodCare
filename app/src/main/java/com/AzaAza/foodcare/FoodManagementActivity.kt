package com.AzaAza.foodcare

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.models.IngredientResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FoodManagementActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val ingredientsList = mutableListOf<Ingredient>()
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
    private val displayDateFormat = SimpleDateFormat("MM월 dd일", Locale.KOREA)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class Ingredient(
        val name: String,
        val location: String,
        val expiryDate: Date,
        val purchaseDate: Date
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_management)

        container = findViewById<LinearLayout>(R.id.ingredientsContainer)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        val searchEditText = findViewById<EditText>(R.id.editTextSearch)

        // 앱 시작 시 서버에서 데이터 불러오기
        fetchIngredientsFromServer()

        // 검색 기능 구현
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterIngredients(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fabAdd.setOnClickListener {
            showAddIngredientDialog()
        }
    }

    private fun fetchIngredientsFromServer() {
        RetrofitClient.ingredientApiService.getIngredients().enqueue(object : Callback<List<IngredientDto>> {
            override fun onResponse(call: Call<List<IngredientDto>>, response: Response<List<IngredientDto>>) {
                if (response.isSuccessful) {
                    val serverIngredients = response.body()
                    if (serverIngredients != null) {
                        // 기존 목록 비우기
                        ingredientsList.clear()
                        container.removeAllViews()

                        // 서버에서 받은 데이터로 목록 채우기
                        for (dto in serverIngredients) {
                            try {
                                val expiryDate = apiDateFormat.parse(dto.expiryDate) ?: Date()
                                val purchaseDate = apiDateFormat.parse(dto.purchaseDate) ?: Date()

                                val ingredient = Ingredient(
                                    dto.name,
                                    dto.location,
                                    expiryDate,
                                    purchaseDate
                                )

                                ingredientsList.add(ingredient)
                                addIngredientCard(ingredient)
                            } catch (e: Exception) {
                                Log.e("FoodManagement", "데이터 파싱 오류", e)
                            }
                        }

                        showToast("서버에서 데이터를 성공적으로 불러왔습니다. (${serverIngredients.size}개)")
                    }
                } else {
                    showToast("서버에서 데이터를 불러오는데 실패했습니다.")
                    Log.e("FoodManagement", "서버 응답 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<IngredientDto>>, t: Throwable) {
                showToast("서버 연결에 실패했습니다.")
                Log.e("FoodManagement", "서버 연결 실패", t)
            }
        })
    }

    private fun filterIngredients(query: String) {
        container.removeAllViews()

        val filteredList = if (query.isEmpty()) {
            ingredientsList
        } else {
            ingredientsList.filter { it.name.contains(query, ignoreCase = true) }
        }

        for (ingredient in filteredList) {
            addIngredientCard(ingredient)
        }
    }

    private fun showAddIngredientDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_ingredient)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.white)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }

        val buttonCancel = dialog.findViewById<Button>(R.id.buttonCancel)
        val buttonSave = dialog.findViewById<Button>(R.id.buttonSave)
        val editTextName = dialog.findViewById<EditText>(R.id.editTextName)
        val editTextLocation = dialog.findViewById<EditText>(R.id.editTextLocation)
        val editTextExpiry = dialog.findViewById<EditText>(R.id.editTextExpiry)
        val editTextPurchase = dialog.findViewById<EditText>(R.id.editTextNotes)

        // DatePicker 처리 추가 - 유통기한
        editTextExpiry.setOnClickListener {
            showDatePickerDialog(editTextExpiry)
        }

        // DatePicker 처리 추가 - 구매날짜
        editTextPurchase.setOnClickListener {
            showDatePickerDialog(editTextPurchase)
        }

        buttonCancel?.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave?.setOnClickListener {
            try {
                val name = editTextName?.text.toString()
                val location = editTextLocation?.text.toString()
                val expiryDateStr = editTextExpiry?.text.toString()
                val purchaseDateStr = editTextPurchase?.text.toString()

                // 기본 유효성 검사
                if (name.isBlank() || location.isBlank() || expiryDateStr.isBlank() || purchaseDateStr.isBlank()) {
                    Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 날짜 파싱
                val expiryDate = dateFormat.parse(expiryDateStr) ?: Date()
                val purchaseDate = dateFormat.parse(purchaseDateStr) ?: Date()

                // 데이터 추가
                val newIngredient = Ingredient(name, location, expiryDate, purchaseDate)

                // 서버에 데이터 전송
                sendIngredientToServer(newIngredient, dialog)

            } catch (e: Exception) {
                Toast.makeText(this, "데이터 형식을 확인해주세요", Toast.LENGTH_SHORT).show()
                Log.e("FoodManagement", "데이터 추가 오류", e)
            }
        }

        dialog.show()
    }

    private fun sendIngredientToServer(ingredient: Ingredient, dialog: Dialog) {
        // 날짜 형식 변환
        val dto = IngredientDto(
            name = ingredient.name,
            location = ingredient.location,
            expiryDate = apiDateFormat.format(ingredient.expiryDate),
            purchaseDate = apiDateFormat.format(ingredient.purchaseDate)
        )

        // 서버로 데이터 전송
        RetrofitClient.ingredientApiService.addIngredient(dto).enqueue(object : Callback<IngredientResponse> {
            override fun onResponse(call: Call<IngredientResponse>, response: Response<IngredientResponse>) {
                if (response.isSuccessful) {
                    // 성공 시 로컬 목록 및 UI 업데이트
                    ingredientsList.add(ingredient)
                    addIngredientCard(ingredient)

                    // 성공 메시지 표시
                    showToast(response.body()?.message ?: "${ingredient.name} 추가 성공!")
                    dialog.dismiss()
                } else {
                    showToast("서버 오류: ${response.code()}")
                    Log.e("FoodManagement", "서버 응답 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<IngredientResponse>, t: Throwable) {
                showToast("서버 연결에 실패했습니다.")
                Log.e("FoodManagement", "서버 연결 실패", t)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = dateFormat.format(selectedDate.time)
                editText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun addIngredientCard(ingredient: Ingredient) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.ingredient_card, container, false) as CardView

        // 카드 내용 설정
        val nameTextView = cardView.findViewById<TextView>(R.id.textViewName)
        val expiryTextView = cardView.findViewById<TextView>(R.id.textViewExpiry)
        val locationTextView = cardView.findViewById<TextView>(R.id.textViewLocation)

        nameTextView.text = ingredient.name

        // 유통기한 계산
        val today = Calendar.getInstance().time
        val expiryStr = displayDateFormat.format(ingredient.expiryDate)
        val purchaseStr = displayDateFormat.format(ingredient.purchaseDate)

        // 유통기한 스타일 설정
        if (ingredient.expiryDate.before(today) || isSameDay(ingredient.expiryDate, today)) {
            expiryTextView.text = "유통기한: 오늘까지"
            expiryTextView.setBackgroundResource(R.drawable.expiry_background)
            expiryTextView.setTextColor(Color.WHITE)
            expiryTextView.setPadding(
                dpToPx(12), dpToPx(4),
                dpToPx(12), dpToPx(4)
            )
        } else {
            expiryTextView.text = "유통기한: $expiryStr"
            expiryTextView.setBackgroundResource(0)
            expiryTextView.setTextColor(Color.parseColor("#666666"))
        }

        locationTextView.text = "${ingredient.location} · 구입 $purchaseStr"

        // 카드를 상단에 추가 (기존 방법 대신)
        container.addView(cardView, 0)  // 인덱스 0에 추가하여 맨 위에 배치
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}