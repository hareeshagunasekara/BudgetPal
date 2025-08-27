package com.example.budgetpal.budgetpal.data

import android.content.Context
import android.util.Patterns
import com.example.budgetpal.budgetpal.data.model.User

class AuthenticationManager(private val context: Context) {
    private val userPreferences = UserPreferences(context)

    fun login(email: String, password: String): AuthResult {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password cannot be empty")
        }

        if (!isValidEmail(email)) {
            return AuthResult.Error("Invalid email format")
        }

        // Get stored user
        val storedUser = userPreferences.getUser()
        if (storedUser == null) {
            return AuthResult.Error("No account found with this email")
        }

        // Check if the email matches
        if (storedUser.email != email) {
            return AuthResult.Error("Invalid credentials")
        }

        // Save the user to mark as logged in
        userPreferences.saveUser(storedUser)
        return AuthResult.Success(storedUser)
    }

    fun signUp(email: String, password: String, username: String): AuthResult {
        // Validate input
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            return AuthResult.Error("All fields are required")
        }

        if (!isValidEmail(email)) {
            return AuthResult.Error("Invalid email format")
        }

        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        if (username.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters")
        }

        // Check if user already exists
        val existingUser = userPreferences.getUser()
        if (existingUser != null && existingUser.email == email) {
            return AuthResult.Error("An account with this email already exists")
        }

        // Create new user
        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = username,
            email = email
        )

        // Save user
        userPreferences.saveUser(newUser)
        return AuthResult.Success(newUser)
    }

    fun logout() {
        userPreferences.clearUser()
    }

    fun getCurrentUser(): User? {
        return userPreferences.getUser()
    }

    fun isLoggedIn(): Boolean {
        return userPreferences.isLoggedIn()
    }
    // Email format validation
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
} 