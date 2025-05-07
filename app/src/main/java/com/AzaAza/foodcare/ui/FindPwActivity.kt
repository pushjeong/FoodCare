package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class FindPwActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pw)

        // 툴바 세팅
        findViewById<TextView>(R.id.topBarTitle).text = "비밀번호 찾기"
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val etUserId = findViewById<EditText>(R.id.etFindUserId)
        findViewById<Button>(R.id.btnFindPw).setOnClickListener {
            val userId = etUserId.text.toString()
            // TODO: 실제 로직 (서버 호출 등) 후 결과 처리
            Toast.makeText(this, "입력하신 아이디로 비밀번호 재설정 링크를 전송했습니다.", Toast.LENGTH_SHORT).show()
        }

    }
}