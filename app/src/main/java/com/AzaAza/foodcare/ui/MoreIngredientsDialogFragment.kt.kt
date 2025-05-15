package com.AzaAza.foodcare.ui

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import kotlinx.coroutines.*

class MoreIngredientsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_more_ingredients)

        val container = dialog.findViewById<LinearLayout>(R.id.moreIngredientsLayout)
        val inflater = LayoutInflater.from(context)
        val closeButton = dialog.findViewById<Button>(R.id.closeButton)

        closeButton.setOnClickListener {
            dismiss()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expenses = RetrofitClient.expenseApiService.getExpenses()
                val groceryExpenses = expenses.filter { it.categoryName == "장보기" }

                val top10 = groceryExpenses
                    .groupingBy { it.productName }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(10)

                val items6to10 = top10.drop(5)

                withContext(Dispatchers.Main) {
                    items6to10.forEachIndexed { index, (name, count) ->
                        val cardView = inflater.inflate(R.layout.item_ingredient_card, container, false)

                        cardView.findViewById<TextView>(R.id.rankCircle).text = "${index + 6}"
                        cardView.findViewById<TextView>(R.id.ingredientName).text = name
                        cardView.findViewById<TextView>(R.id.frequencyText).text = "월 ${count}회 구매"

                        cardView.findViewById<Button>(R.id.addButton).setOnClickListener {
                            Toast.makeText(requireContext(), "$name 을(를) 쇼핑리스트에 추가했습니다.", Toast.LENGTH_SHORT).show()
                        }

                        container.addView(cardView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
