package com.example.budgetpal.budgetpal.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgetpal.budgetpal.data.PreferenceManager

class CurrencyViewModelFactory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 