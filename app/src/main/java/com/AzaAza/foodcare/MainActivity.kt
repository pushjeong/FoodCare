package com.AzaAza.foodcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ 버튼 클릭 시 화면 전환 (식자재관리)
        val btnFoodMgmt = findViewById<ImageButton>(R.id.btnFoodMgmt)
        btnFoodMgmt.setOnClickListener {
            val intent = Intent(this, FoodManagementActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (식자재레시피)
        val btnFoodRecipe = findViewById<ImageButton>(R.id.btnFoodRecipe)
        btnFoodRecipe.setOnClickListener {
            val intent = Intent(this, FoodRecipeActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (외부검색)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            val intent = Intent(this, ExternalSearchActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (냉장고)
        val btnFridge = findViewById<ImageButton>(R.id.btnFridge)
        btnFridge.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (개인정보)
        val btnInformation = findViewById<ImageButton>(R.id.btnInformation)
        btnInformation.setOnClickListener {
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (구성원)
        val btnMember = findViewById<ImageButton>(R.id.btnMember)
        btnMember.setOnClickListener {
            val intent = Intent(this, MemberActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (음식검색)
        val btnFoodSearch = findViewById<ImageButton>(R.id.btnFoodSearch)
        btnFoodSearch.setOnClickListener {
            val intent = Intent(this, FoodSearchActivity::class.java)
            startActivity(intent)
        }
        // ✅ 버튼 클릭 시 화면 전환 (설정)
        val btnSetting = findViewById<ImageButton>(R.id.btnSetting)
        btnSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }


    }
}