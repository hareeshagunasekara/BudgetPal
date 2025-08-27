package com.example.budgetpal.budgetpal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.AuthenticationManager
import com.example.budgetpal.budgetpal.data.AuthResult
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.ui.dashboard.DashboardActivity
import com.example.budgetpal.budgetpal.ui.onboarding.OnboardingActivity
import com.example.budgetpal.budgetpal.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authManager: AuthenticationManager
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authManager = AuthenticationManager(this)
        preferenceManager = PreferenceManager(this)

        checkOnboardingStatus()
        setupClickListeners()
    }

    private fun checkOnboardingStatus() {
        if (!preferenceManager.isOnboardingCompleted()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            if (email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = authManager.signUp(email, password, username)
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 