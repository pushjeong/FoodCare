package com.AzaAza.foodcare.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.adapter.BannerAdapter
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.notification.ExpiryNotificationManager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bannerPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isUserInteracting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)  // 다크모드 무시

        // 알림 권한 확인 및 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, notificationPermission) !=
                PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(notificationPermission),
                    100 // 요청 코드
                )
            }
        }

        // 배터리 최적화 예외 확인 및 요청 - 이 부분을 추가
        ExpiryNotificationManager.checkBatteryOptimization(this)

        // ✅ 상태 바 색상 변경 (Android 11 이상에서만 적용)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false) // Edge-to-edge 활성화
            window.statusBarColor = ContextCompat.getColor(this, R.color.your_background_color)
        }

        // ✅ 상태 바 아이콘 색상 변경 (배경이 어두우면 false, 밝으면 true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false // false = 흰색 아이콘, true = 검은색 아이콘


        // ✅ ViewPager2 설정
        bannerPager = findViewById(R.id.bannerPager)

        val banners = listOf(
            R.drawable.one_banner,
            R.drawable.two_banner,
            R.drawable.three_banner,
            R.drawable.four_banner,
            R.drawable.five_banner
        )

        val adapter = BannerAdapter(banners) { position ->
            val targetActivity = when (position) {
                0 -> ExpenseActivity::class.java
                1 -> UserInfoShowActivity::class.java
                2 -> MemberActivity::class.java
                3 -> FoodManagementActivity::class.java
                4 -> RecipeSearchActivity::class.java
                else -> null
            }
            targetActivity?.let {
                startActivity(Intent(this, it))
            }
        }

        bannerPager.adapter = adapter


        val indicator = findViewById<DotsIndicator>(R.id.dots_indicator)
        indicator.setViewPager2(bannerPager)

        // ✅ 자동 슬라이드 기능
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (!isUserInteracting) {
                    val nextItem = (bannerPager.currentItem + 1) % banners.size
                    bannerPager.currentItem = nextItem
                }
                handler.postDelayed(this, 5000) // 5초마다 실행
            }
        }
        handler.postDelayed(runnable, 5000)

        // ✅ 사용자가 손으로 슬라이드하면 자동 슬라이드 일시 중지
        bannerPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> isUserInteracting = true
                    ViewPager2.SCROLL_STATE_IDLE -> isUserInteracting = false
                }
            }
        })

        // ✅ 버튼들 클릭 이벤트
        findViewById<ImageButton>(R.id.btnFoodMgmt).setOnClickListener {
            startActivity(Intent(this, FoodManagementActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFoodRecipe).setOnClickListener {
            startActivity(Intent(this, RecipeSearchActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, ExpenseAnalysisActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFridge).setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnInformation).setOnClickListener {
            startActivity(Intent(this, UserInfoShowActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnMember).setOnClickListener {
            startActivity(Intent(this, MemberActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFoodSearch).setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSetting).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        // ✅ 레시피 버튼 클릭 이벤트
        findViewById<Button>(R.id.btnViewYukgaejangRecipe).setOnClickListener {
            val intent = Intent(this, RecipeSearchActivity::class.java)
            intent.putExtra("RECIPE_NAME", "육개장")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewShabuShabuRecipe).setOnClickListener {
            val intent = Intent(this, RecipeSearchActivity::class.java)
            intent.putExtra("RECIPE_NAME", "샤브샤브")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewBibimbapRecipe).setOnClickListener {
            val intent = Intent(this, RecipeSearchActivity::class.java)
            intent.putExtra("RECIPE_NAME", "비빔밥")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewKimchiStewRecipe).setOnClickListener {
            val intent = Intent(this, RecipeSearchActivity::class.java)
            intent.putExtra("RECIPE_NAME", "김치찌개")
            startActivity(intent)
        }

        // ✅ 알림 벨 아이콘 클릭 이벤트 추가
        val notificationBell: ImageView = findViewById(R.id.imageView) // 벨 아이콘 ID
        notificationBell.setOnClickListener {
            startActivity(Intent(this, ExpiryNotificationActivity::class.java))
        }

        // 소비기한 알림 배지 업데이트
        updateNotificationBadge()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) { // 알림 권한 요청 코드
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                Log.d("MainActivity", "알림 권한이 허용되었습니다")
            } else {
                // 권한 거부됨
                Log.d("MainActivity", "알림 권한이 거부되었습니다")
                Toast.makeText(
                    this,
                    "알림 권한이 필요합니다. 설정에서 권한을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateNotificationBadge() {
        // 뱃지 UI 요소
        val badgeImage: ImageView = findViewById(R.id.imageView4)
        val badgeText: TextView = findViewById(R.id.textView3)

        // 서버에서 식자재 데이터를 가져와 소비기한이 임박한 항목 카운트
        RetrofitClient.ingredientApiService.getIngredients().enqueue(object : Callback<List<IngredientDto>> {
            override fun onResponse(call: Call<List<IngredientDto>>, response: Response<List<IngredientDto>>) {
                if (response.isSuccessful) {
                    val ingredients = response.body()
                    if (ingredients != null) {
                        // 오늘 날짜 설정
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time

                        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        // 1. 소비기한이 지난 항목
                        val expiredCount = ingredients.count { ingredient ->
                            try {
                                val expiryDate = apiDateFormat.parse(ingredient.expiryDate) ?: return@count false
                                expiryDate.before(today) // 소비기한이 오늘보다 이전이면 expired
                            } catch (e: Exception) {
                                false
                            }
                        }

                        // 2. 소비기한이 3일 이내인 항목 (오늘 포함)
                        val nearExpiryCount = ingredients.count { ingredient ->
                            try {
                                val expiryDate = apiDateFormat.parse(ingredient.expiryDate) ?: return@count false
                                val diffDays = ((expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                                diffDays in 0..3 && !expiryDate.before(today) // 0~3일 이내이고 소비기한이 지나지 않은 것
                            } catch (e: Exception) {
                                false
                            }
                        }

                        // 총 알림 개수 (소비기한 지남 + 소비기한 3일 이내)
                        val totalNotificationCount = expiredCount + nearExpiryCount

                        // 뱃지 표시 로직
                        if (totalNotificationCount > 0) {
                            badgeImage.visibility = View.VISIBLE
                            badgeText.visibility = View.VISIBLE
                            badgeText.text = totalNotificationCount.toString()
                        } else {
                            badgeImage.visibility = View.GONE
                            badgeText.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<IngredientDto>>, t: Throwable) {
                Log.e("MainActivity", "서버 연결 실패", t)
                // 연결 실패 시 뱃지 숨기기
                badgeImage.visibility = View.GONE
                badgeText.visibility = View.GONE
            }
        })
    }

    // ✅ 액티비티 재개 시 데이터와 뱃지 업데이트
    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }

    // ✅ 액티비티 종료 시 자동 슬라이드 정지
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}