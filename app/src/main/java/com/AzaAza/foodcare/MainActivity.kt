package com.AzaAza.foodcare

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class MainActivity : AppCompatActivity() {

    private lateinit var bannerPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isUserInteracting = false

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

        // ✅ ViewPager2 설정
        bannerPager = findViewById(R.id.bannerPager)

        val banners = listOf(
            R.drawable.one_banner,
            R.drawable.two_banner,
            R.drawable.three_banner,
            R.drawable.four_banner,
            R.drawable.five_banner
        )

        val adapter = BannerAdapter(banners)
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
                handler.postDelayed(this, 5000) // 8초마다 실행
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

    // ✅ 액티비티 종료 시 자동 슬라이드 정지
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}