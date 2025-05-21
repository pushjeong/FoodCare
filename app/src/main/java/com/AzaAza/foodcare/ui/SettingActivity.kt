package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { onBackPressed() }

        // 로그아웃
        findViewById<View>(R.id.logoutBar).setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // 프로필 설정
        findViewById<View>(R.id.profileSettingBar).setOnClickListener {
            startActivity(Intent(this, ProfileSettingActivity::class.java))
        }

        // 비밀번호 변경
        findViewById<View>(R.id.passwordChangeBar).setOnClickListener {
            startActivity(Intent(this, PasswordChangeActivity::class.java))
        }

        // 회원 탈퇴
        findViewById<View>(R.id.deleteAccountBar).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    deleteAccount()
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        // 서비스 이용약관
        findViewById<View>(R.id.termsServiceBar).setOnClickListener {
            openWebPage("https://yourdomain.com/terms")
        }

        // 개인정보 처리방침
        findViewById<View>(R.id.privacyPolicyBar).setOnClickListener {
            openWebPage("https://yourdomain.com/privacy")
        }
    }

    private fun deleteAccount() {
        // 실제 API 호출 필요 시 Retrofit 사용
        Toast.makeText(this, "회원 탈퇴되었습니다.", Toast.LENGTH_SHORT).show()
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
