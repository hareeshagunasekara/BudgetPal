package com.example.budgetpal.budgetpal.data.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val description: String = "",
    val date: Date
) 