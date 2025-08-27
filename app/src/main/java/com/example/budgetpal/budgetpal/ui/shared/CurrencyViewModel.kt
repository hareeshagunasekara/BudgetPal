package com.example.budgetpal.budgetpal.ui.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.budgetpal.budgetpal.data.PreferenceManager

class CurrencyViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

    init {
        loadCurrency()
    }

    private fun loadCurrency() {
        _currency.value = preferenceManager.getSelectedCurrency()
    }

    fun updateCurrency(newCurrency: String) {
        preferenceManager.setSelectedCurrency(newCurrency)
        _currency.value = newCurrency
    }
} 