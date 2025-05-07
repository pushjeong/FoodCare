package com.AzaAza.foodcare.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class ExpenseActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var totalExpenseText: TextView
    private lateinit var comparisonTextView: TextView
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var currentMonthText: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton

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

        // 지출 추가 버튼
        val addExpenseButton: FloatingActionButton = findViewById(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener {
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
        val appStartYear = 2023 // 앱 시작 연도를 적절히 설정하세요
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

                withContext(Dispatchers.Main) {
                    categoryAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "카테고리 데이터 로드 실패: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "카테고리 데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val currentMonthData = RetrofitClient.expenseApiService.getMonthlySummary(selectedYear, selectedMonth)
                currentMonthTotal = currentMonthData.totalAmount

                // 파이 차트와 카테고리 목록 업데이트를 위한 데이터
                val categorySummaries = currentMonthData.categories

                // 이전 달 데이터 가져오기
                Log.d("ExpenseActivity", "이전 달 데이터 요청: $previousYear-$previousMonth")
                val previousMonthData = RetrofitClient.expenseApiService.getMonthlySummary(previousYear, previousMonth)
                previousMonthTotal = previousMonthData.totalAmount

                withContext(Dispatchers.Main) {
                    // 총 지출 텍스트 업데이트
                    val formatter = NumberFormat.getInstance(Locale.KOREA)
                    totalExpenseText.text = "${formatter.format(currentMonthTotal.toInt())}원"

                    // 전월 대비 비교 텍스트 업데이트
                    updateComparisonText()

                    // 파이 차트 업데이트
                    updatePieChart(categorySummaries)

                    // 카테고리 목록 업데이트
                    // updateCategoryList(categorySummaries) - 이제 updatePieChart에서 호출함
                }

                // 해당 월의 전체 지출 내역도 로드
                loadExpensesForMonth()
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "월별 데이터 로드 실패: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
            comparisonTextView.setTextColor(resources.getColor(R.color.blue, null))
        } else if (difference < 0) {
            // 전월보다 적게 사용함
            comparisonTextView.text = "- ${formatter.format(abs(difference.toInt()))}"
            comparisonTextView.setTextColor(resources.getColor(R.color.red, null))
        } else {
            // 동일함
            comparisonTextView.text = "변동 없음"
            comparisonTextView.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun updateCategoryList(categorySummaries: List<com.AzaAza.foodcare.api.CategorySummary>, colors: List<Int>? = null) {
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
        }

        // 어댑터 갱신
        categoryAdapter.notifyDataSetChanged()
    }

    private fun updatePieChart(categorySummaries: List<com.AzaAza.foodcare.api.CategorySummary>) {
        val pieEntries = ArrayList<PieEntry>()
        val categoryColors = ArrayList<Int>()

        // 차트 데이터 생성
        for (category in categorySummaries) {
            if (category.amount > 0) {
                pieEntries.add(PieEntry(category.amount.toFloat(), category.categoryName))
            }
        }

        val dataSet = PieDataSet(pieEntries, "지출 카테고리")

        // ColorTemplate에서 색상 가져오기
        val colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.colors = colors

        // 색상 정보를 저장 (카테고리 어댑터에서 사용하기 위해)
        categoryColors.clear()
        categoryColors.addAll(colors)

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
        pieChart.legend.isEnabled = true // 범례 활성화
        pieChart.animateY(1000)
        pieChart.invalidate()

        // 카테고리 어댑터에 색상 정보 전달
        updateCategoryList(categorySummaries, colors)
    }

    private fun showAddExpenseDialog() {
        // 새로운 지출 입력 다이얼로그 표시
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense_new, null)

        val productNameEdit = dialogView.findViewById<EditText>(R.id.productNameEdit)
        val amountEdit = dialogView.findViewById<EditText>(R.id.amountEdit)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val timeButton = dialogView.findViewById<Button>(R.id.timeButton)
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

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        dateButton.text = dateFormat.format(calendar.time)
        timeButton.text = timeFormat.format(calendar.time)

        // 카테고리 스피너 설정
        val categoryNames = categories.map { it.name }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        categorySpinner.adapter = categoryAdapter

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

        // 시간 선택
        timeButton.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                timeButton.text = timeFormat.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

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
                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)
                val selectedCategory = categories[selectedCategoryPos]

                // 서버에 데이터 저장
                saveExpense(selectedCategory.id, productName, amount, dateTime, memo)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveExpense(categoryId: Int, productName: String, amount: Double, dateTime: String, memo: String) {
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
                        Toast.makeText(this@ExpenseActivity, "지출이 추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExpenseActivity, "저장 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCategoryDetailDialog(category: CategoryDto) {
        // 해당 카테고리의 지출 내역만 필터링
        val categoryExpenses = expenses.filter { it.categoryId == category.id }

        if (categoryExpenses.isEmpty()) {
            Toast.makeText(this, "${category.name} 카테고리에 지출 내역이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_detail, null)
        val expenseListView = dialogView.findViewById<ListView>(R.id.expenseListView)
        val categoryTotalText = dialogView.findViewById<TextView>(R.id.categoryTotalText)

        // 총액 표시
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        categoryTotalText.text = "${category.name} 총액: ${formatter.format(category.totalAmount.toInt())}원"

        // 리스트 어댑터 설정
        val adapter = ExpenseListAdapter(this, categoryExpenses)
        expenseListView.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("${category.name} 상세 내역")
            .setView(dialogView)
            .setPositiveButton("확인", null)
            .show()
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

    // 지출 내역 목록용 어댑터 클래스
    inner class ExpenseListAdapter(
        private val context: Context,
        private val expenses: List<ExpenseDto>
    ) : BaseAdapter() {

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

            dateTimeText.text = expense.dateTime

            // 메모가 있는 경우에만 표시
            if (!expense.memo.isNullOrEmpty()) {
                memoText.visibility = View.VISIBLE
                memoText.text = expense.memo
            } else {
                memoText.visibility = View.GONE
            }

            return view
        }
    }
}