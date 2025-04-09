package com.AzaAza.foodcare

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_management)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showAddIngredientDialog()
        }


    }

    private fun showAddIngredientDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_ingredient)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.white)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }

        val buttonCancel = dialog.findViewById<Button>(R.id.buttonCancel)
        val buttonSave = dialog.findViewById<Button>(R.id.buttonSave)
        val editTextName = dialog.findViewById<EditText>(R.id.editTextName)
        val editTextLocation = dialog.findViewById<EditText>(R.id.editTextLocation)
        val editTextExpiry = dialog.findViewById<EditText>(R.id.editTextExpiry)
        val editTextNotes = dialog.findViewById<EditText>(R.id.editTextNotes)

        buttonCancel?.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave?.setOnClickListener {
            val name = editTextName?.text.toString()
            val location = editTextLocation?.text.toString()
            val expiry = editTextExpiry?.text.toString()
            val notes = editTextNotes?.text.toString()

            // TODO: 저장 로직 (원하면 여기에 리스트 갱신 추가)
            dialog.dismiss()
        }

        dialog.show()
    }
}