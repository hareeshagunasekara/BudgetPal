package com.example.budgetpal.budgetpal.adapter

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.model.CategoryBudget
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.*

class CategoryBudgetAdapter(
    private var categories: List<CategoryBudget>,
    private val onCategoryBudgetSaved: (String, Double) -> Unit,
    private val preferenceManager: PreferenceManager
) : RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val budgetInput: TextInputEditText = view.findViewById(R.id.etCategoryBudget)
        val spentAmount: TextView = view.findViewById(R.id.tvSpentAmount)
        val remainingAmount: TextView = view.findViewById(R.id.tvRemainingAmount)
        val progress: ProgressBar = view.findViewById(R.id.progressCategory)
        val saveButton: MaterialButton = view.findViewById(R.id.btnSaveCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_budget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context
        
        holder.categoryName.text = category.category
        holder.budgetInput.setText(if (category.budgetAmount > 0) category.budgetAmount.toString() else "")
        holder.spentAmount.text = "Spent: ${formatCurrency(category.spentAmount)}"
        
        // Update progress bar color based on spending percentage
        val spendingPercentage = if (category.budgetAmount > 0) {
            (category.spentAmount / category.budgetAmount * 100).toInt()
        } else {
            0
        }
        
        // Set progress and color
        holder.progress.progress = spendingPercentage
        val progressColor = when {
            spendingPercentage >= 100 -> ContextCompat.getColor(context, R.color.red_500) // Over budget
            spendingPercentage >= 80 -> ContextCompat.getColor(context, R.color.yellow_500) // Warning
            else -> ContextCompat.getColor(context, R.color.green_500) // Normal
        }
        holder.progress.progressTintList = ColorStateList.valueOf(progressColor)
        
        // Update remaining amount text color based on spending
        val remainingAmount = category.budgetAmount - category.spentAmount
        holder.remainingAmount.apply {
            text = "Remaining: ${formatCurrency(remainingAmount)}"
            setTextColor(when {
                remainingAmount < 0 -> ContextCompat.getColor(context, R.color.red_500)
                remainingAmount <= category.budgetAmount * 0.2 -> ContextCompat.getColor(context, R.color.yellow_500)
                else -> ContextCompat.getColor(context, R.color.green_500)
            })
        }

        // Save button click listener
        holder.saveButton.setOnClickListener {
            val budgetText = holder.budgetInput.text.toString()
            try {
                val budgetAmount = if (budgetText.isNotEmpty()) budgetText.toDouble() else 0.0
                if (budgetAmount < 0) {
                    Toast.makeText(holder.itemView.context, "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                category.budgetAmount = budgetAmount
                onCategoryBudgetSaved(category.category, budgetAmount)
                updateCategoryDisplay(holder, category)
                Toast.makeText(holder.itemView.context, "${category.category} budget updated", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(holder.itemView.context, "Invalid budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        holder.budgetInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val amount = s.toString().toDoubleOrNull() ?: 0.0
                category.budgetAmount = amount
                updateCategoryDisplay(holder, category)
            }
        })
    }

    private fun updateCategoryDisplay(holder: ViewHolder, category: CategoryBudget) {
        val context = holder.itemView.context
        
        // Update amounts
        holder.spentAmount.text = "Spent: ${formatCurrency(category.spentAmount)}"
        val remainingAmount = category.budgetAmount - category.spentAmount
        
        // Calculate spending percentage
        val spendingPercentage = if (category.budgetAmount > 0) {
            (category.spentAmount / category.budgetAmount * 100).toInt()
        } else {
            0
        }
        
        // Update progress bar
        holder.progress.progress = spendingPercentage
        val progressColor = when {
            spendingPercentage >= 100 -> ContextCompat.getColor(context, R.color.red_500)
            spendingPercentage >= 80 -> ContextCompat.getColor(context, R.color.yellow_500)
            else -> ContextCompat.getColor(context, R.color.green_500)
        }
        holder.progress.progressTintList = ColorStateList.valueOf(progressColor)
        
        // Update remaining amount with color
        holder.remainingAmount.apply {
            text = "Remaining: ${formatCurrency(remainingAmount)}"
            setTextColor(when {
                remainingAmount < 0 -> ContextCompat.getColor(context, R.color.red_500)
                remainingAmount <= category.budgetAmount * 0.2 -> ContextCompat.getColor(context, R.color.yellow_500)
                else -> ContextCompat.getColor(context, R.color.green_500)
            })
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val currency = preferenceManager.getSelectedCurrency()
            val locale = when (currency) {
                "USD" -> Locale.US
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                "INR" -> Locale("en", "IN")
                "AUD" -> Locale("en", "AU")
                "CAD" -> Locale("en", "CA")
                "LKR" -> Locale("si", "LK")
                "CNY" -> Locale("zh", "CN")
                "SGD" -> Locale("en", "SG")
                "MYR" -> Locale("ms", "MY")
                "THB" -> Locale("th", "TH")
                "IDR" -> Locale("id", "ID")
                "PHP" -> Locale("en", "PH")
                "VND" -> Locale("vi", "VN")
                "KRW" -> Locale("ko", "KR")
                "AED" -> Locale("ar", "AE")
                "SAR" -> Locale("ar", "SA")
                "QAR" -> Locale("ar", "QA")
                else -> Locale.US
            }
            val format = NumberFormat.getCurrencyInstance(locale)
            format.format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<CategoryBudget>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun getCategoryBudgets(): List<CategoryBudget> = categories
} 