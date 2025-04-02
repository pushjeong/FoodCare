package com.AzaAza.foodcare

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class FridgeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "냉장고"
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