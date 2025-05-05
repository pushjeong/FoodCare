package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R

class FindIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        // 툴바 세팅
        findViewById<TextView>(R.id.topBarTitle).text = "아이디 찾기"
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val etEmail = findViewById<EditText>(R.id.etFindEmail)
        findViewById<Button>(R.id.btnFindId).setOnClickListener {
            val email = etEmail.text.toString()
            // TODO: 실제 로직 (서버 호출 등) 후 결과 처리
            Toast.makeText(this, "입력하신 이메일로 아이디를 전송했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}