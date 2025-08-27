package com.example.budgetpal.budgetpal.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryBudget(
    val category: String,
    var budgetAmount: Double,
    var spentAmount: Double = 0.0
) : Parcelable {
    val remainingAmount: Double
        get() = budgetAmount - spentAmount

    val progress: Int
        get() = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).toInt() else 0
}