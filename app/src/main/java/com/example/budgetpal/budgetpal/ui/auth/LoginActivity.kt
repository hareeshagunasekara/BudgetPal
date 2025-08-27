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
import com.example.budgetpal.budgetpal.data.Result
import com.example.budgetpal.budgetpal.ui.dashboard.DashboardActivity
import com.example.budgetpal.budgetpal.ui.onboarding.OnboardingActivity
import com.example.budgetpal.budgetpal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthenticationManager
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        binding.apply {
            btnLogin.setOnClickListener {
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()

                when (val result = authManager.login(email, password)) {
                    is AuthResult.Success -> {
                        // Navigate to dashboard
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            textSignUp.setOnClickListener {
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            }
        }
    }
} 