package com.AzaAza.foodcare.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
// 툴바 타이틀
        findViewById<TextView>(R.id.topBarTitle).text = "회원가입"
        // 뒤로가기
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val etEmail      = findViewById<EditText>(R.id.etSignEmail)
        val etPw         = findViewById<EditText>(R.id.etSignPw)
        val etPwConfirm  = findViewById<EditText>(R.id.etSignPwConfirm)
        val btnSignUp    = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val email   = etEmail.text.toString()
            val pw      = etPw.text.toString()
            val pwConfirm = etPwConfirm.text.toString()

            when {
                email.isBlank() || pw.isBlank() -> {
                    Toast.makeText(this, "모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                pw != pwConfirm -> {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    prefs.edit()
                        .putString("USER_EMAIL", email)
                        .putString("USER_PW", pw)
                        .apply()
                    Toast.makeText(this, "회원가입 성공! 로그인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}