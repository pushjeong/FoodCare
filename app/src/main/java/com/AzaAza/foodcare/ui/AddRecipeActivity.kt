package com.AzaAza.foodcare.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.RecipeCreateRequest
import com.AzaAza.foodcare.models.RecipeCreateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    // UI 요소들
    private lateinit var editRecipeName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editFoodSummary: EditText
    private lateinit var editIngredients: EditText
    private lateinit var editRecipeInstructions: EditText
    private lateinit var editCookingTime: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var allergyCheckboxGrid: GridLayout
    private lateinit var diseaseCheckboxGrid: GridLayout
    private lateinit var btnRegisterRecipe: Button

    // 사진 관련 UI 요소들
    private lateinit var photoSelectionLayout: LinearLayout
    private lateinit var photoPreviewLayout: FrameLayout
    private lateinit var previewImage: ImageView
    private lateinit var btnCamera: LinearLayout
    private lateinit var btnGallery: LinearLayout
    private lateinit var btnRemovePhoto: ImageView
    private lateinit var errorPhoto: TextView

    // 에러 메시지 TextView들
    private lateinit var errorRecipeName: TextView
    private lateinit var errorCategory: TextView
    private lateinit var errorFoodSummary: TextView
    private lateinit var errorIngredients: TextView
    private lateinit var errorRecipeInstructions: TextView
    private lateinit var errorCookingTime: TextView
    private lateinit var errorDifficulty: TextView
    private lateinit var errorAllergies: TextView
    private lateinit var errorDiseases: TextView

    // 데이터 저장용
    private val selectedAllergies = mutableSetOf<String>()
    private val selectedDiseases = mutableSetOf<String>()

    // 사진 관련 변수들
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String = ""

    // 권한 요청 코드
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101

    // ActivityResultLauncher들
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 카메라에서 촬영한 이미지 처리
            val photoFile = File(currentPhotoPath)
            if (photoFile.exists()) {
                selectedImageUri = Uri.fromFile(photoFile)
                showSelectedImage(selectedImageUri!!)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                showSelectedImage(uri)
            }
        }
    }

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
        setupPhotoListeners()
    }

    private fun initViews() {
        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // EditText들
        editRecipeName = findViewById(R.id.editRecipeName)
        editFoodSummary = findViewById(R.id.editFoodSummary)
        editIngredients = findViewById(R.id.editIngredients)
        editRecipeInstructions = findViewById(R.id.editRecipeInstructions)
        editCookingTime = findViewById(R.id.editCookingTime)

        // Spinner들
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)

        // GridLayout들
        allergyCheckboxGrid = findViewById(R.id.allergyCheckboxGrid)
        diseaseCheckboxGrid = findViewById(R.id.diseaseCheckboxGrid)

        // 버튼
        btnRegisterRecipe = findViewById(R.id.btnRegisterRecipe)

        // 사진 관련 UI 요소들
        photoSelectionLayout = findViewById(R.id.photoSelectionLayout)
        photoPreviewLayout = findViewById(R.id.photoPreviewLayout)
        previewImage = findViewById(R.id.previewImage)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto)
        errorPhoto = findViewById(R.id.errorPhoto)

        // 에러 메시지 TextView들
        errorRecipeName = findViewById(R.id.errorRecipeName)
        errorCategory = findViewById(R.id.errorCategory)
        errorFoodSummary = findViewById(R.id.errorFoodSummary)
        errorIngredients = findViewById(R.id.errorIngredients)
        errorRecipeInstructions = findViewById(R.id.errorRecipeInstructions)
        errorCookingTime = findViewById(R.id.errorCookingTime)
        errorDifficulty = findViewById(R.id.errorDifficulty)
        errorAllergies = findViewById(R.id.errorAllergies)
        errorDiseases = findViewById(R.id.errorDiseases)
    }

    private fun setupPhotoListeners() {
        // 카메라 버튼 클릭
        btnCamera.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // 갤러리 버튼 클릭
        btnGallery.setOnClickListener {
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        // 사진 삭제 버튼 클릭
        btnRemovePhoto.setOnClickListener {
            removeSelectedImage()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 임시 파일 생성
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("AddRecipe", "Error creating image file", ex)
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.AzaAza.foodcare.fileprovider", // AndroidManifest.xml에 정의된 authorities와 일치해야 함
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 타임스탬프를 이용해 고유한 파일명 생성
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("Pictures")
        return File.createTempFile(
            "RECIPE_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showSelectedImage(uri: Uri) {
        try {
            previewImage.setImageURI(uri)
            photoSelectionLayout.visibility = View.GONE
            photoPreviewLayout.visibility = View.VISIBLE
            hideError(errorPhoto)

            Log.d("AddRecipe", "이미지 선택됨: $uri")
        } catch (e: Exception) {
            Log.e("AddRecipe", "이미지 로딩 실패", e)
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeSelectedImage() {
        selectedImageUri = null
        photoSelectionLayout.visibility = View.VISIBLE
        photoPreviewLayout.visibility = View.GONE
        previewImage.setImageURI(null)

        Log.d("AddRecipe", "이미지 제거됨")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSpinner() {
        // 카테고리 스피너 설정
        val categories = arrayOf(
            "카테고리를 선택하세요",
            "한식",
            "양식",
            "일식",
            "중식",
            "아시아",
            "디저트"
        )

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    hideError(errorCategory)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 난이도 스피너 설정
        val difficultyLevels = arrayOf(
            "난이도를 선택하세요",
            "쉬움 ⭐",
            "보통 ⭐⭐",
            "어려움 ⭐⭐⭐",
            "매우 어려움 ⭐⭐⭐⭐"
        )

        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyLevels)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = difficultyAdapter

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

        // 음식 설명 텍스트 변경 감지
        editFoodSummary.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    hideError(errorFoodSummary)
                    resetEditTextStyle(editFoodSummary)
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

        // 레시피 설명 텍스트 변경 감지
        editRecipeInstructions.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    hideError(errorRecipeInstructions)
                    resetEditTextStyle(editRecipeInstructions)
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

        // 사진 검증 (선택사항이지만 필요시 활성화)
        // if (selectedImageUri == null) {
        //     showError(errorPhoto, "음식 사진을 등록해주세요.")
        //     isValid = false
        // }

        // 레시피 이름 검증
        if (editRecipeName.text.toString().trim().isEmpty()) {
            showError(errorRecipeName, "레시피 이름을 입력해주세요.")
            setEditTextError(editRecipeName)
            isValid = false
        }

        // 카테고리 검증
        if (spinnerCategory.selectedItemPosition == 0) {
            showError(errorCategory, "카테고리를 선택해주세요.")
            isValid = false
        }

        // 음식 설명 검증
        if (editFoodSummary.text.toString().trim().isEmpty()) {
            showError(errorFoodSummary, "음식 설명을 입력해주세요.")
            setEditTextError(editFoodSummary)
            isValid = false
        }

        // 재료 검증
        if (editIngredients.text.toString().trim().isEmpty()) {
            showError(errorIngredients, "필요한 재료를 입력해주세요.")
            setEditTextError(editIngredients)
            isValid = false
        }

        // 레시피 설명 검증
        if (editRecipeInstructions.text.toString().trim().isEmpty()) {
            showError(errorRecipeInstructions, "레시피 설명을 입력해주세요.")
            setEditTextError(editRecipeInstructions)
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
        // 버튼 비활성화 (중복 클릭 방지)
        btnRegisterRecipe.isEnabled = false
        btnRegisterRecipe.text = "등록 중..."

        // 카테고리 변환
        val categoryText = when (spinnerCategory.selectedItemPosition) {
            1 -> "한식"
            2 -> "양식"
            3 -> "일식"
            4 -> "중식"
            5 -> "아시아"
            6 -> "디저트"
            else -> "한식" // 기본값
        }

        // 난이도 레벨 변환
        val difficultyLevel = when (spinnerDifficulty.selectedItemPosition) {
            1 -> "쉬움"
            2 -> "보통"
            3 -> "어려움"
            4 -> "매우 어려움"
            else -> "보통"
        }

        // 알레르기 정보를 문자열로 변환
        val allergiesString = if (selectedAllergies.contains("none")) {
            "없음"
        } else {
            selectedAllergies.joinToString(", ") { key ->
                allergyOptions.find { it.first == key }?.second ?: key
            }
        }

        // 질병 정보를 문자열로 변환
        val diseaseString = if (selectedDiseases.contains("normal")) {
            "일반 건강식"
        } else {
            selectedDiseases.joinToString(", ") { key ->
                diseaseOptions.find { it.first == key }?.second ?: key
            }
        }

        // 질병 이유 생성 (선택된 질병에 따라)
        val diseaseReason = if (selectedDiseases.contains("normal")) {
            "일반적인 건강한 식단을 위한 레시피입니다."
        } else {
            "해당 질병을 가진 분들의 건강 관리에 도움이 되는 레시피입니다."
        }

        // API 요청 객체 생성 (새로운 필드 순서)
        val request = RecipeCreateRequest(
            name = editRecipeName.text.toString().trim(),
            summary = editFoodSummary.text.toString().trim(),  // 음식 설명
            ingredients = editIngredients.text.toString().trim(),
            instructions = editRecipeInstructions.text.toString().trim(),  // 레시피 설명
            timetaken = "${editCookingTime.text}분",
            difficultylevel = difficultyLevel,
            allergies = allergiesString,
            disease = diseaseString,
            diseasereason = diseaseReason,
            category = categoryText  // 선택된 카테고리
        )

        // 🔍 디버깅: 요청 데이터 로깅
        Log.d("AddRecipe", "=== 레시피 등록 요청 ===")
        Log.d("AddRecipe", "name: ${request.name}")
        Log.d("AddRecipe", "summary: ${request.summary}")
        Log.d("AddRecipe", "ingredients: ${request.ingredients}")
        Log.d("AddRecipe", "instructions: ${request.instructions}")
        Log.d("AddRecipe", "timetaken: ${request.timetaken}")
        Log.d("AddRecipe", "difficultylevel: ${request.difficultylevel}")
        Log.d("AddRecipe", "allergies: ${request.allergies}")
        Log.d("AddRecipe", "disease: ${request.disease}")
        Log.d("AddRecipe", "diseasereason: ${request.diseasereason}")
        Log.d("AddRecipe", "category: ${request.category}")
        Log.d("AddRecipe", "selectedImageUri: $selectedImageUri")
        Log.d("AddRecipe", "========================")

        // API 호출
        RetrofitClient.recipeApiService.createRecipe(request).enqueue(object : Callback<RecipeCreateResponse> {
            override fun onResponse(
                call: Call<RecipeCreateResponse>,
                response: Response<RecipeCreateResponse>
            ) {
                // 버튼 다시 활성화
                btnRegisterRecipe.isEnabled = true
                btnRegisterRecipe.text = "레시피 등록"

                // 🔍 디버깅: 응답 상태 로깅
                Log.d("AddRecipe", "=== 서버 응답 ===")
                Log.d("AddRecipe", "HTTP 코드: ${response.code()}")
                Log.d("AddRecipe", "응답 성공여부: ${response.isSuccessful}")
                Log.d("AddRecipe", "응답 메시지: ${response.message()}")

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("AddRecipe", "응답 body: $result")
                    Log.d("AddRecipe", "result.success: ${result?.success}")
                    Log.d("AddRecipe", "result.message: ${result?.message}")
                    Log.d("AddRecipe", "result.recipeId: ${result?.recipeId}")

                    if (result?.success == true) {
                        // 성공 처리
                        Toast.makeText(
                            this@AddRecipeActivity,
                            "레시피가 성공적으로 등록되었습니다! (ID: ${result.recipeId})",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("AddRecipe", "✅ 레시피 등록 성공: ID = ${result.recipeId}")

                        // 폼 초기화
                        clearForm()
                    } else {
                        // 서버에서 실패 응답
                        Log.e("AddRecipe", "❌ 서버에서 실패 응답")
                        Toast.makeText(
                            this@AddRecipeActivity,
                            "레시피 등록 실패: ${result?.message ?: "알 수 없는 오류"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // HTTP 오류
                    Log.e("AddRecipe", "❌ HTTP 오류: ${response.code()}")

                    // 응답 본문도 확인
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AddRecipe", "에러 응답 본문: $errorBody")
                    } catch (e: Exception) {
                        Log.e("AddRecipe", "에러 응답 본문 읽기 실패: $e")
                    }

                    Toast.makeText(
                        this@AddRecipeActivity,
                        "서버 오류가 발생했습니다. (코드: ${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.d("AddRecipe", "==================")
            }

            override fun onFailure(call: Call<RecipeCreateResponse>, t: Throwable) {
                // 버튼 다시 활성화
                btnRegisterRecipe.isEnabled = true
                btnRegisterRecipe.text = "레시피 등록"

                // 🔍 디버깅: 네트워크 오류 상세 로깅
                Log.e("AddRecipe", "❌ 네트워크 오류 발생", t)
                Log.e("AddRecipe", "오류 메시지: ${t.message}")
                Log.e("AddRecipe", "오류 타입: ${t.javaClass.simpleName}")

                // 네트워크 오류 처리
                Toast.makeText(
                    this@AddRecipeActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun clearForm() {
        editRecipeName.text.clear()
        spinnerCategory.setSelection(0)
        editFoodSummary.text.clear()
        editIngredients.text.clear()
        editRecipeInstructions.text.clear()
        editCookingTime.text.clear()
        spinnerDifficulty.setSelection(0)

        // 선택된 이미지 제거
        removeSelectedImage()

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
        errorCategory.visibility = View.GONE
        errorFoodSummary.visibility = View.GONE
        errorIngredients.visibility = View.GONE
        errorRecipeInstructions.visibility = View.GONE
        errorCookingTime.visibility = View.GONE
        errorDifficulty.visibility = View.GONE
        errorAllergies.visibility = View.GONE
        errorDiseases.visibility = View.GONE
        errorPhoto.visibility = View.GONE
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
}