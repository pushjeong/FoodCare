package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.AzaAza.foodcare.R
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.AzaAza.foodcare.data.ShoppingList

class ExpenseAnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_analysis)

        // 뒤로가기 버튼
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // “쇼핑리스트 확인하기” 버튼
        val checkShoppingBT = findViewById<Button>(R.id.checkShoppingListButton)
        checkShoppingBT.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        // 1~5등 아이템 “추가” 버튼들
        val addButtons = listOf(
            R.id.addToShoppingListButton1 to R.id.itemNameText1,
            R.id.addToShoppingListButton2 to R.id.itemNameText2,
            R.id.addToShoppingListButton3 to R.id.itemNameText3,
            R.id.addToShoppingListButton4 to R.id.itemNameText4,
            R.id.addToShoppingListButton5 to R.id.itemNameText5
        )
        for ((btnId, nameTvId) in addButtons) {
            findViewById<Button>(btnId).setOnClickListener {
                val itemName = findViewById<TextView>(nameTvId).text.toString()
                // 1) 쇼핑리스트에 추가
                val newCount = ShoppingList.addItem(itemName)
                // 2) 간단히 토스트로만 알림
                Toast.makeText(
                    this,
                    "$itemName 구매 갯수: $newCount 개",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // “쇼핑리스트 확인하기” 버튼은 그대로
        findViewById<Button>(R.id.checkShoppingListButton).setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}