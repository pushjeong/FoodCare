package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 뒤로가기
        findViewById<ImageView>(R.id.backButton)
            .setOnClickListener { onBackPressed() }

        // 로그아웃 바 클릭 처리
        findViewById<View>(R.id.logoutBar)
            .setOnClickListener {
                // 1) SharedPreferences 초기화
                val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .remove("IS_LOGGED_IN")
                    .remove("USER_EMAIL")
                    .remove("USER_PW")
                    .apply()

                // 2) 로그인 화면으로 (스택 클리어)
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}