package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.AzaAza.foodcare.R

class AddRecipeActivity : AppCompatActivity() {

    // UI 요소들
    private lateinit var editRecipeName: EditText
    private lateinit var editRecipeDescription: EditText
    private lateinit var editIngredients: EditText
    private lateinit var editCookingTime: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var allergyCheckboxGrid: GridLayout
    private lateinit var diseaseCheckboxGrid: GridLayout
    private lateinit var btnRegisterRecipe: Button

    // 에러 메시지 TextView들
    private lateinit var errorRecipeName: TextView
    private lateinit var errorRecipeDescription: TextView
    private lateinit var errorIngredients: TextView
    private lateinit var errorCookingTime: TextView
    private lateinit var errorDifficulty: TextView
    private lateinit var errorAllergies: TextView
    private lateinit var errorDiseases: TextView

    // 데이터 저장용
    private val selectedAllergies = mutableSetOf<String>()
    private val selectedDiseases = mutableSetOf<String>()

    // 알레르기 옵션들
    private val allergyOptions = arrayOf(
        "poultry" to "난류(가금류)",
        "milk" to "우유",
        "buckwheat" to "메밀",
        "peanut" to "땅콩",
        "soy" to "대두",
        "wheat" to "밀",
        "mackerel" to "고등어",
        "crab" to "게",
        "shrimp" to "새우",
        "pork" to "돼지고기",
        "peach" to "복숭아",
        "tomato" to "토마토",
        "sulfites" to "아황산류",
        "walnut" to "호두",
        "chicken" to "닭고기",
        "beef" to "쇠고기",
        "squid" to "오징어",
        "shellfish" to "조개류",
        "pine_nuts" to "잣",
        "none" to "알레르기 유발 요소 없음"
    )

    // 질병 옵션들
    private val diseaseOptions = arrayOf(
        "heart_disease" to "심장 질환",
        "hyperlipidemia" to "고지혈증(고콜레스테롤혈증)",
        "kidney_disease" to "신장 질환",
        "indigestion" to "소화불량",
        "food_poisoning" to "식중독",
        "gastric_disorder" to "위장 질환",
        "celiac_disease" to "셀리악병",
        "asthma" to "천식",
        "gout" to "통풍",
        "lactose_intolerance" to "유당불내증",
        "hypertension" to "고혈압",
        "diabetes" to "당뇨병",
        "obesity" to "비만",
        "normal" to "일반 건강식"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        initViews()
        setupSpinner()
        setupCheckboxes()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun initViews() {
        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // EditText들
        editRecipeName = findViewById(R.id.editRecipeName)
        editRecipeDescription = findViewById(R.id.editRecipeDescription)
        editIngredients = findViewById(R.id.editIngredients)
        editCookingTime = findViewById(R.id.editCookingTime)

        // Spinner
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)

        // GridLayout들
        allergyCheckboxGrid = findViewById(R.id.allergyCheckboxGrid)
        diseaseCheckboxGrid = findViewById(R.id.diseaseCheckboxGrid)

        // 버튼
        btnRegisterRecipe = findViewById(R.id.btnRegisterRecipe)

        // 에러 메시지 TextView들
        errorRecipeName = findViewById(R.id.errorRecipeName)
        errorRecipeDescription = findViewById(R.id.errorRecipeDescription)
        errorIngredients = findViewById(R.id.errorIngredients)
        errorCookingTime = findViewById(R.id.errorCookingTime)
        errorDifficulty = findViewById(R.id.errorDifficulty)
        errorAllergies = findViewById(R.id.errorAllergies)
        errorDiseases = findViewById(R.id.errorDiseases)
    }

    private fun setupSpinner() {
        val difficultyLevels = arrayOf(
            "난이도를 선택하세요",
            "쉬움 ⭐",
            "보통 ⭐⭐",
            "어려움 ⭐⭐⭐",
            "매우 어려움 ⭐⭐⭐⭐"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = adapter

        spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    hideError(errorDifficulty)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCheckboxes() {
        // 알레르기 체크박스 생성
        allergyOptions.forEach { (key, label) ->
            val checkBox = CheckBox(this).apply {
                text = label
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@AddRecipeActivity, R.color.gray_700))

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(0, 8, 16, 8)
                }
                layoutParams = params

                setOnCheckedChangeListener { _, isChecked ->
                    handleAllergySelection(key, isChecked)
                }
            }
            allergyCheckboxGrid.addView(checkBox)
        }

        // 질병 체크박스 생성
        diseaseOptions.forEach { (key, label) ->
            val checkBox = CheckBox(this).apply {
                text = label
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@AddRecipeActivity, R.color.gray_700))

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(0, 8, 16, 8)
                }
                layoutParams = params

                setOnCheckedChangeListener { _, isChecked ->
                    handleDiseaseSelection(key, isChecked)
                }
            }
            diseaseCheckboxGrid.addView(checkBox)
        }
    }

    private fun handleAllergySelection(key: String, isChecked: Boolean) {
        if (key == "none") {
            if (isChecked) {
                // "없음" 선택 시 다른 모든 알레르기 해제
                selectedAllergies.clear()
                selectedAllergies.add("none")
                uncheckOtherAllergies("none")
            } else {
                selectedAllergies.remove("none")
            }
        } else {
            if (isChecked) {
                // 다른 알레르기 선택 시 "없음" 해제
                selectedAllergies.add(key)
                if (selectedAllergies.contains("none")) {
                    selectedAllergies.remove("none")
                    uncheckAllergyCheckbox("none")
                }
            } else {
                selectedAllergies.remove(key)
            }
        }

        if (selectedAllergies.isNotEmpty()) {
            hideError(errorAllergies)
        }
    }

    private fun handleDiseaseSelection(key: String, isChecked: Boolean) {
        if (key == "normal") {
            if (isChecked) {
                // "일반 건강식" 선택 시 다른 모든 질병 해제
                selectedDiseases.clear()
                selectedDiseases.add("normal")
                uncheckOtherDiseases("normal")
            } else {
                selectedDiseases.remove("normal")
            }
        } else {
            if (isChecked) {
                // 다른 질병 선택 시 "일반 건강식" 해제
                selectedDiseases.add(key)
                if (selectedDiseases.contains("normal")) {
                    selectedDiseases.remove("normal")
                    uncheckDiseaseCheckbox("normal")
                }
            } else {
                selectedDiseases.remove(key)
            }
        }

        if (selectedDiseases.isNotEmpty()) {
            hideError(errorDiseases)
        }
    }

    private fun uncheckOtherAllergies(exceptKey: String) {
        for (i in 0 until allergyCheckboxGrid.childCount) {
            val checkBox = allergyCheckboxGrid.getChildAt(i) as CheckBox
            val key = allergyOptions[i].first
            if (key != exceptKey) {
                checkBox.isChecked = false
            }
        }
    }

    private fun uncheckOtherDiseases(exceptKey: String) {
        for (i in 0 until diseaseCheckboxGrid.childCount) {
            val checkBox = diseaseCheckboxGrid.getChildAt(i) as CheckBox
            val key = diseaseOptions[i].first
            if (key != exceptKey) {
                checkBox.isChecked = false
            }
        }
    }

    private fun uncheckAllergyCheckbox(key: String) {
        val index = allergyOptions.indexOfFirst { it.first == key }
        if (index >= 0 && index < allergyCheckboxGrid.childCount) {
            val checkBox = allergyCheckboxGrid.getChildAt(index) as CheckBox
            checkBox.isChecked = false
        }
    }

    private fun uncheckDiseaseCheckbox(key: String) {
        val index = diseaseOptions.indexOfFirst { it.first == key }
        if (index >= 0 && index < diseaseCheckboxGrid.childCount) {
            val checkBox = diseaseCheckboxGrid.getChildAt(index) as CheckBox
            checkBox.isChecked = false
        }
    }

    private fun setupTextWatchers() {
        // 레시피 이름 텍스트 변경 감지
        editRecipeName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    hideError(errorRecipeName)
                    resetEditTextStyle(editRecipeName)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 레시피 설명 텍스트 변경 감지
        editRecipeDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    hideError(errorRecipeDescription)
                    resetEditTextStyle(editRecipeDescription)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 재료 텍스트 변경 감지
        editIngredients.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    hideError(errorIngredients)
                    resetEditTextStyle(editIngredients)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 소요 시간 텍스트 변경 감지
        editCookingTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val time = s.toString().toIntOrNull()
                if (time != null && time > 0) {
                    hideError(errorCookingTime)
                    resetEditTextStyle(editCookingTime)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupClickListeners() {
        btnRegisterRecipe.setOnClickListener {
            if (validateForm()) {
                registerRecipe()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // 레시피 이름 검증
        if (editRecipeName.text.toString().trim().isEmpty()) {
            showError(errorRecipeName, "레시피 이름을 입력해주세요.")
            setEditTextError(editRecipeName)
            isValid = false
        }

        // 레시피 설명 검증
        if (editRecipeDescription.text.toString().trim().isEmpty()) {
            showError(errorRecipeDescription, "레시피 설명을 입력해주세요.")
            setEditTextError(editRecipeDescription)
            isValid = false
        }

        // 재료 검증
        if (editIngredients.text.toString().trim().isEmpty()) {
            showError(errorIngredients, "필요한 재료를 입력해주세요.")
            setEditTextError(editIngredients)
            isValid = false
        }

        // 소요 시간 검증
        val cookingTime = editCookingTime.text.toString().toIntOrNull()
        if (cookingTime == null || cookingTime <= 0) {
            showError(errorCookingTime, "소요 시간을 올바르게 입력해주세요.")
            setEditTextError(editCookingTime)
            isValid = false
        }

        // 난이도 검증
        if (spinnerDifficulty.selectedItemPosition == 0) {
            showError(errorDifficulty, "난이도를 선택해주세요.")
            isValid = false
        }

        // 알레르기 정보 검증
        if (selectedAllergies.isEmpty()) {
            showError(errorAllergies, "알레르기 관련 정보를 선택해주세요.")
            isValid = false
        }

        // 질병 정보 검증
        if (selectedDiseases.isEmpty()) {
            showError(errorDiseases, "질병 관련 정보를 선택해주세요.")
            isValid = false
        }

        return isValid
    }

    private fun registerRecipe() {
        // 레시피 데이터 수집
        val recipeData = RecipeData(
            name = editRecipeName.text.toString().trim(),
            description = editRecipeDescription.text.toString().trim(),
            ingredients = editIngredients.text.toString().trim(),
            cookingTime = editCookingTime.text.toString().toInt(),
            difficulty = spinnerDifficulty.selectedItemPosition,
            allergies = selectedAllergies.toList(),
            diseases = selectedDiseases.toList()
        )

        // TODO: 데이터베이스에 저장하는 로직 구현
        // 예: RecipeRepository.insertRecipe(recipeData)

        // 임시로 로그 출력
        println("레시피 등록 데이터: $recipeData")

        // 성공 메시지 표시
        Toast.makeText(this, "레시피가 성공적으로 등록되었습니다!", Toast.LENGTH_LONG).show()

        // 폼 초기화 또는 액티비티 종료
        clearForm()
        // 또는 finish()
    }

    private fun clearForm() {
        editRecipeName.text.clear()
        editRecipeDescription.text.clear()
        editIngredients.text.clear()
        editCookingTime.text.clear()
        spinnerDifficulty.setSelection(0)

        // 모든 체크박스 해제
        for (i in 0 until allergyCheckboxGrid.childCount) {
            val checkBox = allergyCheckboxGrid.getChildAt(i) as CheckBox
            checkBox.isChecked = false
        }

        for (i in 0 until diseaseCheckboxGrid.childCount) {
            val checkBox = diseaseCheckboxGrid.getChildAt(i) as CheckBox
            checkBox.isChecked = false
        }

        selectedAllergies.clear()
        selectedDiseases.clear()

        // 모든 에러 메시지 숨기기
        hideAllErrors()
    }

    private fun showError(errorTextView: TextView, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError(errorTextView: TextView) {
        errorTextView.visibility = View.GONE
    }

    private fun hideAllErrors() {
        errorRecipeName.visibility = View.GONE
        errorRecipeDescription.visibility = View.GONE
        errorIngredients.visibility = View.GONE
        errorCookingTime.visibility = View.GONE
        errorDifficulty.visibility = View.GONE
        errorAllergies.visibility = View.GONE
        errorDiseases.visibility = View.GONE
    }

    private fun setEditTextError(editText: EditText) {
        editText.background = ContextCompat.getDrawable(this, R.drawable.edit_text_error)
    }

    private fun resetEditTextStyle(editText: EditText) {
        editText.background = ContextCompat.getDrawable(this, R.drawable.edit_text_modern)
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

    // 데이터 클래스
    data class RecipeData(
        val name: String,
        val description: String,
        val ingredients: String,
        val cookingTime: Int,
        val difficulty: Int,
        val allergies: List<String>,
        val diseases: List<String>
    )
}