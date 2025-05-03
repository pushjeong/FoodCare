package com.AzaAza.foodcare.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.adapter.ShoppingListAdapter
import com.AzaAza.foodcare.data.ShoppingList
import com.google.android.material.appbar.MaterialToolbar

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var topBarTitle: TextView
    private lateinit var rvShoppingList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // 뷰 바인딩
        backButton    = findViewById(R.id.backButton)
        topBarTitle   = findViewById(R.id.topBarTitle)
        rvShoppingList = findViewById(R.id.rvShoppingList)

        // 뒤로가기 버튼 동작
        backButton.setOnClickListener { finish() }

        // 타이틀(필요시 동적으로 변경)
        topBarTitle.text = "쇼핑리스트"

        // 인텐트로 전달된 아이템 추가
        intent.getStringExtra("addedItem")?.let {
            ShoppingList.addItem(it)
        }

        // RecyclerView 세팅
        rvShoppingList.layoutManager = LinearLayoutManager(this)
        rvShoppingList.adapter = ShoppingListAdapter(ShoppingList.items.toList())
    }

    override fun onResume() {
        super.onResume()
        // 데이터 변경 시 어댑터에 반영
        (rvShoppingList.adapter as? ShoppingListAdapter)
            ?.updateData(ShoppingList.items.toList())
    }
}