package com.AzaAza.foodcare.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.models.LoginRequest
import com.AzaAza.foodcare.models.UserResponse
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.LoginResponse
import com.AzaAza.foodcare.models.UpdateFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
        val etLoginId = findViewById<EditText>(R.id.etLoginId)  // 로그인 아이디 입력
        val etPw      = findViewById<EditText>(R.id.etLoginPw)  // 비밀번호 입력

        val btnLogin  = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnGoSignUp)
        val btnFindId = findViewById<Button>(R.id.btnFindId)
        val btnFindPw = findViewById<Button>(R.id.btnFindPw)

        // 로그인 처리
        btnLogin.setOnClickListener {
            val loginId = etLoginId.text.toString().trim()
            val pw      = etPw.text.toString().trim()

            val req = LoginRequest(login_id = loginId, password = pw)  // 수정된 요청

            RetrofitClient.userApiService.login(req).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        prefs.edit()
                            .putBoolean("IS_LOGGED_IN", true)
                            .putString("USER_LOGIN_ID", body.login_id ?: "")
                            .putInt("USER_ID", body.id ?: 0)
                            .apply()

                        // 👇 로그인 직후 FCM 토큰 강제 전송
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                Log.d("FCM", "로그인 직후 강제 토큰 전송: $token, loginId=${body.login_id}")
                                if (body.login_id != null) {
                                    val req = UpdateFcmTokenRequest(login_id = body.login_id, fcm_token = token)
                                    RetrofitClient.userApiService.updateFcmToken(req)
                                        .enqueue(object : retrofit2.Callback<Void> {
                                            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                                                Log.d("FCM", "서버로 FCM 토큰 저장 성공")
                                            }
                                            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                                                Log.e("FCM", "서버로 FCM 토큰 저장 실패: ${t.message}")
                                            }
                                        })
                                }
                            } else {
                                Log.e("FCM", "FCM 토큰 받아오기 실패: ${task.exception}")
                            }
                        }

                        // 기존 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, body?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

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