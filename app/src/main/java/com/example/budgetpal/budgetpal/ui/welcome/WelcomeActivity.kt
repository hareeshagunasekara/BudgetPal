package com.example.budgetpal.budgetpal.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.ui.auth.LoginActivity
import com.example.budgetpal.budgetpal.ui.auth.SignUpActivity
import com.example.budgetpal.budgetpal.ui.onboarding.OnboardingActivity
import com.example.budgetpal.budgetpal.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val TAG = "WelcomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d(TAG, "onCreate: Starting WelcomeActivity")
            super.onCreate(savedInstanceState)
            
            try {
                binding = ActivityWelcomeBinding.inflate(layoutInflater)
                Log.d(TAG, "onCreate: Binding inflated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: Error inflating binding", e)
                throw e
            }
            
            setContentView(binding.root)
            Log.d(TAG, "onCreate: Content view set")

            try {
                preferenceManager = PreferenceManager(this)
                Log.d(TAG, "onCreate: PreferenceManager initialized")
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: Error initializing PreferenceManager", e)
                throw e
            }

            checkOnboardingStatus()
            setupClickListeners()
            Log.d(TAG, "onCreate: Completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Fatal error", e)
            throw e
        }
    }

    private fun checkOnboardingStatus() {
        try {
            Log.d(TAG, "checkOnboardingStatus: Checking onboarding status")
            val isCompleted = preferenceManager.isOnboardingCompleted()
            Log.d(TAG, "checkOnboardingStatus: Onboarding completed = $isCompleted")
            
            if (!isCompleted) {
                Log.d(TAG, "checkOnboardingStatus: Starting OnboardingActivity")
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkOnboardingStatus: Error", e)
        }
    }

    private fun setupClickListeners() {
        try {
            Log.d(TAG, "setupClickListeners: Setting up click listeners")
            binding.btnLogin.setOnClickListener {
                Log.d(TAG, "setupClickListeners: Login button clicked")
                startActivity(Intent(this, LoginActivity::class.java))
            }

            binding.btnSignUp.setOnClickListener {
                Log.d(TAG, "setupClickListeners: SignUp button clicked")
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            Log.d(TAG, "setupClickListeners: Click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "setupClickListeners: Error", e)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity started")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed")
    }
} 