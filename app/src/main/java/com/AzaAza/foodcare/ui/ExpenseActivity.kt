package com.AzaAza.foodcare.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.adapter.CategoryAdapter
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.CategoryDto
import com.AzaAza.foodcare.models.ExpenseDto
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import com.AzaAza.foodcare.adapter.ExpenseGroupAdapter
import com.AzaAza.foodcare.models.ExpenseGroup

class ExpenseActivity : AppCompatActivity() {

    // 고정된 카테고리 색상 맵 추가
    companion object {
        // 고정된 카테고리 색상 맵 (카테고리 이름 -> 색상)
        val CATEGORY_COLORS = mapOf(
            "외식" to Color.parseColor("#5B8FF9"),   // 푸른색
            "배달" to Color.parseColor("#FF6B3B"),   // 주황색
            "주류" to Color.parseColor("#FFD666"),   // 노란색
            "장보기" to Color.parseColor("#65D1AA"), // 연두색
            "간식" to Color.parseColor("#F28CB1"),   // 분홍색
            "기타" to Color.parseColor("#8A67E8")    // 보라색
        )
    }

    private lateinit var pieChart: PieChart
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var totalExpenseText: TextView
    private lateinit var comparisonTextView: TextView
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var currentMonthText: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton

    // 탭 관련 UI 요소
    private lateinit var personalModeTab: LinearLayout
    private lateinit var sharedModeTab: LinearLayout
    private lateinit var personalModeIcon: ImageView
    private lateinit var sharedModeIcon: ImageView
    private lateinit var personalModeText: TextView
    private lateinit var sharedModeText: TextView
    private lateinit var sharedModeInfo: LinearLayout
    private lateinit var sharedModeInfoText: TextView

    private val categories = ArrayList<CategoryDto>()
    private val expenses = ArrayList<ExpenseDto>()
    private var totalExpense: Double = 0.0
    private val chartColors = ArrayList<Int>()

    private var currentMonthTotal: Double = 0.0
    private var previousMonthTotal: Double = 0.0

    // 현재 선택된 연도와 월
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

    // 현재 실제 연도와 월 (현재 날짜)
    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    // 현재 모드 (개인/공유)
    private var isSharedMode: Boolean = false

    // 현재 대화상자의 어댑터를 추적하기 위한 변수 추가
    /* 미사용으로 삭제 됨
    private var currentExpenseListAdapter: ExpenseListAdapter? =null
    */

    private var currentGroupAdapter: ExpenseGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // UI 초기화
        pieChart = findViewById(R.id.expensePieChart)
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        totalExpenseText = findViewById(R.id.totalExpenseText)
        comparisonTextView = findViewById(R.id.comparisonTextView)

        // 탭 UI 초기화
        personalModeTab = findViewById(R.id.personalModeTab)
        sharedModeTab = findViewById(R.id.sharedModeTab)
        personalModeIcon = findViewById(R.id.personalModeIcon)
        sharedModeIcon = findViewById(R.id.sharedModeIcon)
        personalModeText = findViewById(R.id.personalModeText)
        sharedModeText = findViewById(R.id.sharedModeText)
        sharedModeInfo = findViewById(R.id.sharedModeInfo)
        sharedModeInfoText = sharedModeInfo.findViewById(R.id.sharedModeInfoText)
        // 현재 날짜로 초기화
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1

        // 선택된 연도와 월을 현재 연도와 월로 초기화
        selectedYear = currentYear
        selectedMonth = currentMonth

        // 연월 선택 UI 초기화
        currentMonthText = findViewById(R.id.currentMonthText)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)

        updateMonthYearText()
        updateNextButtonState()
        updateModeUI()

        // 탭 이벤트 리스너 설정
        personalModeTab.setOnClickListener {
            switchToPersonalMode()
        }

        sharedModeTab.setOnClickListener {
            switchToSharedMode()
        }

        // 이벤트 리스너 설정
        currentMonthText.setOnClickListener {
            showMonthYearPicker()
        }

        prevMonthButton.setOnClickListener {
            moveToPreviousMonth()
        }

        nextMonthButton.setOnClickListener {
            moveToNextMonth()
        }

        val fabAddExpense: View = findViewById(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        // RecyclerView 설정
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(categories) { category ->
            // 카테고리 클릭 시 세부 내역 화면으로 이동
            showCategoryDetailDialog(category)
        }
        categoriesRecyclerView.adapter = categoryAdapter

        // 카테고리 데이터 로드
        loadCategories()

        // 선택된 달의 지출 내역 로드
        loadMonthlyData()
    }

    private fun switchToPersonalMode() {
        if (!isSharedMode) return

        isSharedMode = false
        updateModeUI()
        loadMonthlyData() // 데이터 새로고침
    }

    private fun switchToSharedMode() {
        if (isSharedMode) return

        isSharedMode = true
        updateModeUI()
        loadMonthlyData() // 데이터 새로고침
    }

    private fun updateModeUI() {
        // 공유 모드 안내 텍스트의 마진을 동적으로 조정
        val sharedModeInfoLayoutParams = sharedModeInfo.layoutParams as ViewGroup.MarginLayoutParams

        if (isSharedMode) {
            // 개인 모드 비활성화
            personalModeTab.setBackgroundColor(Color.TRANSPARENT)
            personalModeIcon.setColorFilter(Color.parseColor("#999999"))
            personalModeText.setTextColor(Color.parseColor("#999999"))

            // 공유 모드 활성화
            sharedModeTab.setBackgroundResource(R.drawable.bg_toggle_selected_green_rect)
            sharedModeIcon.setColorFilter(Color.parseColor("#00C471"))
            sharedModeText.setTextColor(Color.parseColor("#00C471"))

            sharedModeInfo.visibility = View.VISIBLE

            // 공유 모드일 때 상단 마진 증가
            sharedModeInfoLayoutParams.topMargin = dpToPx(16) // 16dp
            sharedModeInfoLayoutParams.bottomMargin = dpToPx(-4) // 하단 마진을 음수로 설정하여 아래 요소를 위로 당김
        } else {
            // 공유 모드 비활성화
            sharedModeTab.setBackgroundColor(Color.TRANSPARENT)
            sharedModeIcon.setColorFilter(Color.parseColor("#999999"))
            sharedModeText.setTextColor(Color.parseColor("#999999"))

            // 개인 모드 활성화
            personalModeTab.setBackgroundResource(R.drawable.bg_toggle_selected_blue_rect)
            personalModeIcon.setColorFilter(Color.parseColor("#007AFF"))
            personalModeText.setTextColor(Color.parseColor("#007AFF"))

            sharedModeInfo.visibility = View.GONE

            // 개인 모드일 때는 기본 마진
            sharedModeInfoLayoutParams.topMargin = dpToPx(8) // 기본값
            sharedModeInfoLayoutParams.bottomMargin = dpToPx(4) // 기본값
        }

        // 마진 변경 적용
        sharedModeInfo.layoutParams = sharedModeInfoLayoutParams
    }

    // dp를 px로 변환하는 helper 메서드
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun updateMonthYearText() {
        currentMonthText.text = "${selectedYear}년 ${selectedMonth}월"
    }

    private fun updateNextButtonState() {
        // 현재 월이 선택되었을 때 다음 버튼을 비활성화
        if (selectedYear == currentYear && selectedMonth == currentMonth) {
            nextMonthButton.isEnabled = false
            nextMonthButton.alpha = 0.5f  // 시각적으로 비활성화되었음을 표시
        } else {
            nextMonthButton.isEnabled = true
            nextMonthButton.alpha = 1.0f
        }
    }

    private fun moveToPreviousMonth() {
        if (selectedMonth == 1) {
            selectedYear--
            selectedMonth = 12
        } else {
            selectedMonth--
        }
        updateMonthYearText()
        updateNextButtonState()
        loadMonthlyData()
    }

    private fun moveToNextMonth() {
        // 현재 월보다 더 미래로 갈 수 없도록 체크
        if (selectedYear == currentYear && selectedMonth == currentMonth) {
            return
        }

        if (selectedMonth == 12) {
            selectedYear++
            selectedMonth = 1
        } else {
            selectedMonth++
        }
        updateMonthYearText()
        updateNextButtonState()
        loadMonthlyData()
    }

    private fun showMonthYearPicker() {
        // 연월 선택 다이얼로그 표시
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        // 월 설정 (1~12)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = selectedMonth

        // 연도 설정 (앱 시작 연도부터 현재 연도까지)

        val appStartYear = 2024
        yearPicker.minValue = appStartYear
        yearPicker.maxValue = currentYear
        yearPicker.value = selectedYear

        AlertDialog.Builder(this)
            .setTitle("연월 선택")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val newYear = yearPicker.value
                val newMonth = monthPicker.value

                // 미래 날짜 선택 방지
                if (newYear > currentYear || (newYear == currentYear && newMonth > currentMonth)) {
                    Toast.makeText(this, "미래 날짜는 선택할 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                selectedYear = newYear
                selectedMonth = newMonth
                updateMonthYearText()
                updateNextButtonState()
                loadMonthlyData()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 카테고리 데이터 가져오기
                Log.d("ExpenseActivity", "카테고리 데이터 요청 시작")
                val categoryResponse = RetrofitClient.expenseApiService.getCategories()
                Log.d("ExpenseActivity", "카테고리 데이터 응답: ${categoryResponse.size}개")

                categories.clear()
                categories.addAll(categoryResponse)

                // '기타' 카테고리가 없는 경우 추가
                if (categories.none { it.name == "기타" }) {
                    // 기타 카테고리 추가 (ID는 서버에서 할당되므로 임시로 큰 값 사용)
                    val etcCategory = CategoryDto(
                        id = 999,
                        name = "기타",
                        icon = null,
                        totalAmount = 0.0
                    )
                    categories.add(etcCategory)

                }

                withContext(Dispatchers.Main) {
                    categoryAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "카테고리 데이터 로드 실패: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExpenseActivity,
                        "카테고리 데이터 로드 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadMonthlyData() {
        // 이전 달 계산
        val previousCalendar = Calendar.getInstance()
        previousCalendar.set(selectedYear, selectedMonth - 1, 1)
        previousCalendar.add(Calendar.MONTH, -1)
        val previousYear = previousCalendar.get(Calendar.YEAR)
        val previousMonth = previousCalendar.get(Calendar.MONTH) + 1

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 선택한 달 데이터 가져오기
                Log.d("ExpenseActivity", "선택한 달 데이터 요청: $selectedYear-$selectedMonth")
                val currentMonthData =
                    RetrofitClient.expenseApiService.getMonthlySummary(selectedYear, selectedMonth)
                currentMonthTotal = currentMonthData.totalAmount

                // 파이 차트와 카테고리 목록 업데이트를 위한 데이터
                val categorySummaries = currentMonthData.categories

                // 이전 달 데이터 가져오기
                Log.d("ExpenseActivity", "이전 달 데이터 요청: $previousYear-$previousMonth")
                val previousMonthData =
                    RetrofitClient.expenseApiService.getMonthlySummary(previousYear, previousMonth)
                previousMonthTotal = previousMonthData.totalAmount

                withContext(Dispatchers.Main) {
                    // 총 지출 텍스트 업데이트
                    val formatter = NumberFormat.getInstance(Locale.KOREA)
                    totalExpenseText.text = "${formatter.format(currentMonthTotal.toInt())}원"

                    // 전월 대비 비교 텍스트 업데이트
                    updateComparisonText()

                    // 파이 차트 업데이트 (현재는 숨김 처리)
                    updatePieChart(categorySummaries)

                    // 카테고리 목록 업데이트
                    updateCategoryList(categorySummaries)
                }

                // 해당 월의 전체 지출 내역도 로드
                loadExpensesForMonth()
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "월별 데이터 로드 실패: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExpenseActivity,
                        "데이터 로드 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadExpensesForMonth() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 현재 선택된 달의 지출 내역 데이터 가져오기
                val expenseResponse = RetrofitClient.expenseApiService.getExpenses()

                // 해당 달의 데이터만 필터링
                val selectedMonthStr = String.format("%d-%02d", selectedYear, selectedMonth)
                val filteredExpenses = expenseResponse.filter {
                    it.dateTime.startsWith(selectedMonthStr)
                }

                expenses.clear()
                expenses.addAll(filteredExpenses)

                Log.d("ExpenseActivity", "선택한 달 지출 내역: ${expenses.size}개")
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "지출 내역 로드 실패: ${e.message}", e)
            }
        }
    }

    private fun updateComparisonText() {
        val difference = currentMonthTotal - previousMonthTotal
        val formatter = NumberFormat.getInstance(Locale.KOREA)

        if (difference > 0) {
            // 전월보다 많이 사용함
            comparisonTextView.text = "+ ${formatter.format(difference.toInt())}"
            comparisonTextView.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else if (difference < 0) {
            // 전월보다 적게 사용함
            comparisonTextView.text = "- ${formatter.format(abs(difference.toInt()))}"
            comparisonTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            // 동일함
            comparisonTextView.text = "변동 없음"
            comparisonTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun updateCategoryList(
        categorySummaries: List<com.AzaAza.foodcare.api.CategorySummary>,
        colors: List<Int>? = null
    ) {
        // 카테고리 목록 업데이트
        for (category in categories) {
            // 초기화
            category.totalAmount = 0.0

            // 해당 카테고리 데이터 찾기
            val summaryCat = categorySummaries.find { it.categoryName == category.name }
            if (summaryCat != null) {
                category.totalAmount = summaryCat.amount
            }
        }

        // 색상 정보 전달
        if (colors != null && colors.isNotEmpty()) {
            categoryAdapter.updateColors(colors)
        } else {
            // 기본 색상 맵 사용
            val categoryAdapterColors = categories.map { category ->
                CATEGORY_COLORS[category.name] ?: Color.GRAY
            }
            categoryAdapter.updateColors(categoryAdapterColors)
        }

        // 어댑터 갱신
        categoryAdapter.notifyDataSetChanged()
    }

    private fun updatePieChart(categorySummaries: List<com.AzaAza.foodcare.api.CategorySummary>) {
        if (categorySummaries.sumOf { it.amount } <= 0.0) {
            pieChart.visibility = View.GONE
            return
        } else {
            pieChart.visibility = View.VISIBLE
        }
        val pieEntries = ArrayList<PieEntry>()
        val categoryColors = ArrayList<Int>()

        // 차트 데이터 생성
        for (category in categorySummaries) {
            if (category.amount > 0) {
                pieEntries.add(PieEntry(category.amount.toFloat(), category.categoryName))

                // 고정된 색상 맵에서 색상 가져오기
                val color = CATEGORY_COLORS[category.categoryName] ?: Color.GRAY
                categoryColors.add(color)
            }
        }

        // 범례 텍스트 없이 데이터셋 생성
        val dataSet = PieDataSet(pieEntries, "")  // 빈 문자열로 설정하여 범례 텍스트 제거

        // 위에서 생성한 색상 목록 사용
        dataSet.colors = categoryColors

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)

        // 텍스트 완전히 제거
        pieData.setDrawValues(false)

        // 차트 내부 값 표시 비활성화
        pieChart.setDrawEntryLabels(false)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "지출 분포"
        pieChart.setCenterTextSize(16f)
        pieChart.legend.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate()

        // 카테고리 목록에도 같은 색상 정보 전달
        val categoryAdapterColors = categories.map { category ->
            CATEGORY_COLORS[category.name] ?: Color.GRAY
        }
        updateCategoryList(categorySummaries, categoryAdapterColors)
    }

    private fun showAddExpenseDialog() {
        // 새로운 지출 입력 다이얼로그 표시
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense_new, null)

        val productNameEdit = dialogView.findViewById<EditText>(R.id.productNameEdit)
        val amountEdit = dialogView.findViewById<EditText>(R.id.amountEdit)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val memoEdit = dialogView.findViewById<EditText>(R.id.memoEdit)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        // 현재 선택된 달로 날짜 설정
        val calendar = Calendar.getInstance()
        // 선택된 연도와 월이 현재 연월과 같으면 현재 일자, 아니면 해당 월의 1일로 설정
        if (selectedYear == currentYear && selectedMonth == currentMonth) {
            // 현재 날짜 사용
        } else {
            // 선택된 달의 1일로 설정
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        }

        // 시간 부분 기본값 설정 (00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateButton.text = dateFormat.format(calendar.time)

        // 날짜 선택
        dateButton.setOnClickListener {
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                // 선택된 날짜가 현재 달의 범위 내에 있는지 확인
                if (year == selectedYear && month + 1 == selectedMonth) {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    dateButton.text = dateFormat.format(calendar.time)
                } else {
                    Toast.makeText(this, "선택한 달의 날짜만 입력할 수 있습니다", Toast.LENGTH_SHORT).show()
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), currentDay).apply {
                // DatePicker의 범위를 선택된 월의 범위로 제한
                datePicker.minDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth - 1, 1)
                }.timeInMillis

                datePicker.maxDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth - 1, getActualMaximum(Calendar.DAY_OF_MONTH))
                    // 현재 달이면 현재 날짜까지만 선택 가능하도록 제한
                    if (selectedYear == currentYear && selectedMonth == currentMonth) {
                        set(currentYear, currentMonth - 1, get(Calendar.DAY_OF_MONTH))
                    }
                }.timeInMillis
            }.show()
        }

        // 카테고리 스피너 설정
        val categoryNames = categories.map { it.name }
        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        categorySpinner.adapter = categoryAdapter

        AlertDialog.Builder(this)
            .setTitle("지출 입력")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val productName = productNameEdit.text.toString()
                val amountText = amountEdit.text.toString()
                val memo = memoEdit.text.toString()
                val selectedCategoryPos = categorySpinner.selectedItemPosition

                if (productName.isEmpty() || amountText.isEmpty() || selectedCategoryPos == -1) {
                    Toast.makeText(this, "상품명, 금액, 카테고리를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountText.replace(",", "").toDoubleOrNull() ?: 0.0
                // 시간은 기본값(00:00)으로 설정하여 날짜만 사용
                val dateTime =
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)
                val selectedCategory = categories[selectedCategoryPos]

                // 서버에 데이터 저장
                saveExpense(selectedCategory.id, productName, amount, dateTime, memo)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveExpense(
        categoryId: Int,
        productName: String,
        amount: Double,
        dateTime: String,
        memo: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 새로운 지출 객체 생성 - 서버에서 ID가 생성되므로 임시 ID 0 사용
                val expense = ExpenseDto(
                    id = 0,
                    categoryId = categoryId,
                    productName = productName,
                    amount = amount,
                    dateTime = dateTime,
                    memo = memo
                )

                // API 호출
                val response = RetrofitClient.expenseApiService.addExpense(expense)

                if (response.isSuccessful) {
                    // 월별 데이터 다시 로드
                    loadMonthlyData()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExpenseActivity, "지출이 추가되었습니다", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExpenseActivity,
                            "저장 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // showCategoryDetailDialog 메서드 수정
    private fun showCategoryDetailDialog(category: CategoryDto) {
        val categoryExpenses = expenses.filter { it.categoryId == category.id }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_detail, null)
        val expenseGroupRecyclerView = dialogView.findViewById<RecyclerView>(R.id.expenseGroupRecyclerView)
        val categoryNameText = dialogView.findViewById<TextView>(R.id.categoryNameText)
        val categoryNameTextSmall = dialogView.findViewById<TextView>(R.id.categoryNameTextSmall)
        val totalAmountText = dialogView.findViewById<TextView>(R.id.totalAmountText)
        val emptyStateLayout = dialogView.findViewById<LinearLayout>(R.id.emptyStateLayout)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)

        // 카테고리 이름 설정
        categoryNameText.text = category.name
        categoryNameTextSmall.text = category.name

        // 총 금액 설정
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        totalAmountText.text = "${formatter.format(category.totalAmount.toInt())}원"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 다이얼로그 배경을 투명하게 설정하고 둥근 모서리 적용
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 닫기 버튼 이벤트 처리
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        if (categoryExpenses.isEmpty()) {
            // 지출 내역이 없는 경우
            expenseGroupRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            // 지출 내역이 있는 경우
            expenseGroupRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE

            val expenseGroups = groupExpensesByDate(categoryExpenses)

            // 모든 그룹을 확장된 상태로 설정 (헤더가 없으므로)
            expenseGroups.forEach { it.isExpanded = true }

            expenseGroupRecyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = ExpenseGroupAdapter(expenseGroups) { expense ->
                showDeleteConfirmDialog(expense)
            }
            expenseGroupRecyclerView.adapter = adapter
            currentGroupAdapter = adapter

            Toast.makeText(this, "항목을 길게 누르면 삭제할 수 있습니다", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // 지출 항목 삭제 확인 다이얼로그
    private fun showDeleteConfirmDialog(expense: ExpenseDto) {
        AlertDialog.Builder(this)
            .setTitle("지출 항목 삭제")
            .setMessage(
                "'${expense.productName}' 항목을 삭제하시겠습니까?\n금액: ${
                    NumberFormat.getInstance(
                        Locale.KOREA
                    ).format(expense.amount.toInt())
                }원"
            )
            .setPositiveButton("삭제") { _, _ ->
                deleteExpense(expense.id)
            }
            .setNegativeButton("취소", null)
            .show()
    }


    // 지출 내역을 날짜별로 그룹화하는 helper 메서드
    private fun groupExpensesByDate(expenses: List<ExpenseDto>): MutableList<ExpenseGroup> {
        val result = mutableListOf<ExpenseGroup>()
        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.add(Calendar.DATE, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // 날짜별로 그룹화
        val dateMap = expenses.groupBy {
            it.dateTime.split(" ")[0]  // "yyyy-MM-dd HH:mm"에서 "yyyy-MM-dd" 추출
        }

        // 날짜 역순으로 정렬 (최신 날짜가 먼저 오도록)
        val sortedDates = dateMap.keys.sortedDescending()

        for (date in sortedDates) {
            val displayTitle = when (date) {
                today -> "오늘"
                yesterday -> "어제"
                else -> {
                    val dateParts = date.split("-")
                    if (dateParts.size == 3) {
                        // 요일 계산
                        val year = dateParts[0].toInt()
                        val month = dateParts[1].toInt() - 1 // 월은 0-based
                        val day = dateParts[2].toInt()

                        val cal = Calendar.getInstance()
                        cal.set(year, month, day)

                        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.SUNDAY -> "일"
                            Calendar.MONDAY -> "월"
                            Calendar.TUESDAY -> "화"
                            Calendar.WEDNESDAY -> "수"
                            Calendar.THURSDAY -> "목"
                            Calendar.FRIDAY -> "금"
                            Calendar.SATURDAY -> "토"
                            else -> ""
                        }

                        "$date ($dayOfWeek)"
                    } else {
                        date
                    }
                }
            }

            val group = ExpenseGroup(
                date = date,
                displayTitle = displayTitle,
                expenses = dateMap[date]?.toMutableList() ?: mutableListOf(),
                isExpanded = false
            )

            result.add(group)
        }

        // 첫 번째 그룹은 기본적으로 펼침
        if (result.isNotEmpty()) {
            result[0].isExpanded = true
        }

        return result
    }

    // 지출 항목 삭제 API 호출
    private fun deleteExpense(expenseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.expenseApiService.deleteExpense(expenseId)

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        // 삭제된 항목 expenseId 기반으로 제거
                        expenses.removeAll { it.id == expenseId }

                        // 현재 상세 다이얼로그 어댑터에 삭제 반영
                        currentGroupAdapter?.removeExpense(expenseId)

                        // 상단 카드(카테고리 금액 등) 갱신
                        loadMonthlyData()

                        Toast.makeText(this@ExpenseActivity, "지출 항목이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExpenseActivity, "삭제 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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


/* 미사용으로 삭제 됨
// 지출 내역 목록용 어댑터 클래스 수정
inner class ExpenseListAdapter(
    private val context: Context,
    private val expenses: MutableList<ExpenseDto> // List에서 MutableList로 변경
) : BaseAdapter() {

    // 새로운 메서드 추가: 지출 항목 제거
    fun removeExpense(expenseId: Int) {
        val position = expenses.indexOfFirst { it.id == expenseId }
        if (position != -1) {
            expenses.removeAt(position)
            notifyDataSetChanged()
        }
    }
    override fun getCount(): Int = expenses.size

    override fun getItem(position: Int): Any = expenses[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_expense, parent, false)

        val expense = expenses[position]

        val productNameText: TextView = view.findViewById(R.id.productNameText)
        val amountText: TextView = view.findViewById(R.id.amountText)
        val dateTimeText: TextView = view.findViewById(R.id.dateTimeText)
        val memoText: TextView = view.findViewById(R.id.memoText)

        productNameText.text = expense.productName

        // 금액 포맷팅
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        amountText.text = "${formatter.format(expense.amount.toInt())}원"

        // 날짜 부분만 추출해서 표시
        val dateOnly = expense.dateTime.split(" ")[0] // "yyyy-MM-dd HH:mm"에서 "yyyy-MM-dd" 추출
        dateTimeText.text = dateOnly

        // 메모가 있는 경우에만 표시
        if (!expense.memo.isNullOrEmpty()) {
            memoText.visibility = View.VISIBLE
            memoText.text = expense.memo
        } else {
            memoText.visibility = View.GONE
        }

        // 아이템 롱클릭 리스너 추가
        view.setOnLongClickListener {
            showDeleteConfirmDialog(expense)
            true
        }

        return view
    }
}*/
}