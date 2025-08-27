package com.example.budgetpal

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.ui.dashboard.DashboardActivity
import com.example.budgetpal.budgetpal.ui.onboarding.OnboardingActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val preferenceManager = PreferenceManager(this)
        if (!preferenceManager.isOnboardingCompleted()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        finish()
    }
}