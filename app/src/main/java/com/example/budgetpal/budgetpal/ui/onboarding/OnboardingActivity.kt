package com.example.budgetpal.budgetpal.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.ui.auth.LoginActivity
import com.example.budgetpal.budgetpal.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewPagerAdapter: OnboardingViewPagerAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)

        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        viewPagerAdapter = OnboardingViewPagerAdapter()
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.btnNext.text = if (position == viewPagerAdapter.itemCount - 1) "Get Started" else "Next"
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem == viewPagerAdapter.itemCount - 1) {
                startLoginActivity()
            } else {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            }
        }

        binding.btnSkip.setOnClickListener {
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
        preferenceManager.setOnboardingCompleted(true)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}