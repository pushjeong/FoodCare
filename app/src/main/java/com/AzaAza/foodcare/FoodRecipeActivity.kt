package com.AzaAza.foodcare

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class FoodRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_recipe)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "식자재 레시피"
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