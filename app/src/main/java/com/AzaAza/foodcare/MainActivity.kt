package com.AzaAza.foodcare


import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import android.os.Handler
import android.os.Looper



class MainActivity : AppCompatActivity() {

    private lateinit var bannerPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val adapter = bannerPager.adapter
            if (adapter != null && adapter.itemCount > 0) {
                val nextPage = (bannerPager.currentItem + 1) % adapter.itemCount
                bannerPager.setCurrentItem(nextPage, true)
                handler.postDelayed(this, 3000) // 3초마다 변경
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // ✅ 상태 바 색상 변경 (Android 11 이상에서만 적용)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false) // Edge-to-edge 활성화
            window.statusBarColor = ContextCompat.getColor(this, R.color.your_background_color)
        }

        // ✅ 상태 바 아이콘 색상 변경 (배경이 어두우면 false, 밝으면 true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false // false = 흰색 아이콘, true = 검은색 아이콘

        setupButtonClickListeners()


        bannerPager = findViewById(R.id.bannerPager)

        val banners = listOf(
            R.drawable.one_banner,
            R.drawable.two_banner,
            R.drawable.three_banner,
            R.drawable.five_banner,

        )

        val adapter = BannerAdapter(banners)
        bannerPager.adapter = adapter

        if (banners.isNotEmpty()) {
            startAutoScroll()
        }

        // 사용자가 배너를 슬라이드하는 동안 자동 슬라이드 멈춤
        bannerPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> stopAutoScroll() // 사용자가 스와이프하는 동안 중지
                    ViewPager2.SCROLL_STATE_IDLE -> startAutoScroll() // 손을 떼면 다시 시작
                }
            }
        })
    }


    private fun setupButtonClickListeners() {
        findViewById<ImageButton>(R.id.btnFoodMgmt).setOnClickListener {
            startActivity(Intent(this, FoodManagementActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFoodRecipe).setOnClickListener {
            startActivity(Intent(this, FoodRecipeActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, ExternalSearchActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFridge).setOnClickListener {
            startActivity(Intent(this, FridgeActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnInformation).setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnMember).setOnClickListener {
            startActivity(Intent(this, MemberActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFoodSearch).setOnClickListener {
            startActivity(Intent(this, FoodSearchActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSetting).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }
    private fun stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable) // 자동 슬라이드 중지
    }
    private fun startAutoScroll() {
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoScrollRunnable) // 메모리 누수 방지
    }
}


