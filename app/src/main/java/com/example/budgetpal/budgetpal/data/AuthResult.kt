package com.example.budgetpal.budgetpal.data

import com.example.budgetpal.budgetpal.data.model.User

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
} 