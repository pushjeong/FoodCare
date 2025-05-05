package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1) 툴바 타이틀 세팅
        val tvTitle = findViewById<TextView>(R.id.topBarTitle)
        tvTitle.text = "로그인"

        // 2) 뒤로가기 버튼
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val etEmail   = findViewById<EditText>(R.id.etLoginEmail)
        val etPw      = findViewById<EditText>(R.id.etLoginPw)
        val btnLogin  = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnGoSignUp)
        val btnFindId = findViewById<Button>(R.id.btnFindId)
        val btnFindPw = findViewById<Button>(R.id.btnFindPw)

        // 로그인 처리
        btnLogin.setOnClickListener {
            val email     = etEmail.text.toString()
            val pw        = etPw.text.toString()
            val savedEmail= prefs.getString("USER_EMAIL", null)
            val savedPw   = prefs.getString("USER_PW", null)

            if (email == savedEmail && pw == savedPw) {
                prefs.edit().putBoolean("IS_LOGGED_IN", true).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "이메일 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 화면 이동
        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 아이디 찾기 화면 이동
        btnFindId.setOnClickListener {
            startActivity(Intent(this, FindIdActivity::class.java))
        }

        // 비밀번호 찾기 화면 이동
        btnFindPw.setOnClickListener {
            startActivity(Intent(this, FindPwActivity::class.java))
        }
    }
}