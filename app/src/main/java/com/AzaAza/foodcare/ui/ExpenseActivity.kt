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

    private var currentMonthTotal: Double = 0.0
    private var previousMonthTotal: Double = 0.0

    // 현재 선택된 연도와 월
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

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
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1

        // 연월 선택 UI 초기화
        currentMonthText = findViewById(R.id.currentMonthText)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)

        updateMonthYearText()

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
            showCategorySelectionDialog()
        }

        // RecyclerView 설정
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(categories) { category ->
            // 카테고리 클릭 시 세부 내역 화면으로 이동
            showCategoryDetailDialog(category)
        }
        categoriesRecyclerView.adapter = categoryAdapter

        // 데이터 로드
        loadData()
        loadMonthlyData()
    }

    private fun updateMonthYearText() {
        currentMonthText.text = "${selectedYear}년 ${selectedMonth}월"
    }

    private fun moveToPreviousMonth() {
        if (selectedMonth == 1) {
            selectedYear--
            selectedMonth = 12
        } else {
            selectedMonth--
        }
        updateMonthYearText()
        loadMonthlyData()
    }

    private fun moveToNextMonth() {
        if (selectedMonth == 12) {
            selectedYear++
            selectedMonth = 1
        } else {
            selectedMonth++
        }
        updateMonthYearText()
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
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val appStartYear = 2023 // 앱 시작 연도를 적절히 설정하세요
        yearPicker.minValue = appStartYear
        yearPicker.maxValue = currentYear
        yearPicker.value = selectedYear

        AlertDialog.Builder(this)
            .setTitle("연월 선택")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                selectedYear = yearPicker.value
                selectedMonth = monthPicker.value
                updateMonthYearText()
                loadMonthlyData()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 카테고리 데이터 가져오기
                Log.d("ExpenseActivity", "카테고리 데이터 요청 시작")
                val categoryResponse = RetrofitClient.expenseApiService.getCategories()
                Log.d("ExpenseActivity", "카테고리 데이터 응답: ${categoryResponse.size}개")

                categories.clear()
                categories.addAll(categoryResponse)

                // 지출 내역 가져오기
                Log.d("ExpenseActivity", "지출 내역 데이터 요청 시작")
                val expenseResponse = RetrofitClient.expenseApiService.getExpenses()
                Log.d("ExpenseActivity", "지출 내역 데이터 응답: ${expenseResponse.size}개")

                expenses.clear()
                expenses.addAll(expenseResponse)

                // 카테고리별 총액 계산
                calculateTotalExpense()

                withContext(Dispatchers.Main) {
                    // UI 업데이트
                    updatePieChart()
                    categoryAdapter.notifyDataSetChanged()
                    updateTotalExpenseText()
                }
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "데이터 로드 실패: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseActivity, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Log.d("ExpenseActivity", "선택한 달 총액: $currentMonthTotal")

                // 이전 달 데이터 가져오기
                Log.d("ExpenseActivity", "이전 달 데이터 요청: $previousYear-$previousMonth")
                val previousMonthData = RetrofitClient.expenseApiService.getMonthlySummary(previousYear, previousMonth)
                previousMonthTotal = previousMonthData.totalAmount
                Log.d("ExpenseActivity", "이전 달 총액: $previousMonthTotal")

                withContext(Dispatchers.Main) {
                    updateComparisonText()
                    // 총 지출 텍스트 업데이트
                    val formatter = NumberFormat.getInstance(Locale.KOREA)
                    totalExpenseText.text = "${formatter.format(currentMonthTotal.toInt())}원"
                }
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "월별 데이터 로드 실패: ${e.message}", e)
            }
        }
    }

    private fun calculateTotalExpense() {
        totalExpense = 0.0

        // 카테고리별 총액 계산
        for (category in categories) {
            category.totalAmount = 0.0
            for (expense in expenses) {
                if (expense.categoryId == category.id) {
                    category.totalAmount += expense.amount
                    totalExpense += expense.amount
                }
            }
        }
    }

    private fun updateTotalExpenseText() {
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        totalExpenseText.text = "${formatter.format(totalExpense.toInt())}원"
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

    private fun updatePieChart() {
        val pieEntries = ArrayList<PieEntry>()

        // 차트 데이터 생성
        for (category in categories) {
            if (category.totalAmount > 0) {
                pieEntries.add(PieEntry(category.totalAmount.toFloat(), category.name))
            }
        }

        val dataSet = PieDataSet(pieEntries, "지출 카테고리")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "지출 분포"
        pieChart.setCenterTextSize(16f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun showCategorySelectionDialog() {
        val categoryNames = categories.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("카테고리 선택")
            .setItems(categoryNames) { _, which ->
                val selectedCategory = categories[which]
                showExpenseInputDialog(selectedCategory)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showExpenseInputDialog(category: CategoryDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)

        val productNameEdit = dialogView.findViewById<EditText>(R.id.productNameEdit)
        val amountEdit = dialogView.findViewById<EditText>(R.id.amountEdit)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val timeButton = dialogView.findViewById<Button>(R.id.timeButton)
        val memoEdit = dialogView.findViewById<EditText>(R.id.memoEdit)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        dateButton.text = dateFormat.format(calendar.time)
        timeButton.text = timeFormat.format(calendar.time)

        // 날짜 선택
        dateButton.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                dateButton.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
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
            .setTitle("${category.name} 지출 입력")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val productName = productNameEdit.text.toString()
                val amountText = amountEdit.text.toString()
                val memo = memoEdit.text.toString()

                if (productName.isEmpty() || amountText.isEmpty()) {
                    Toast.makeText(this, "상품명과 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountText.replace(",", "").toDoubleOrNull() ?: 0.0
                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)

                // 서버에 데이터 저장
                saveExpense(category.id, productName, amount, dateTime, memo)
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
                    // 데이터 다시 로드
                    loadData()
                    // 월별 데이터도 다시 로드
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