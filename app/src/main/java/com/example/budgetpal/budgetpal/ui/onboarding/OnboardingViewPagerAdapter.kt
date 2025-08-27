package com.example.budgetpal.budgetpal.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.databinding.ItemOnboardingBinding

class OnboardingViewPagerAdapter : RecyclerView.Adapter<OnboardingViewPagerAdapter.OnboardingViewHolder>() {

    private val onboardingItems = listOf(
        OnboardingItem(
            "Say hi to your new finance tracker",
            "You’re amazing for taking this first step towards getting better control over your money and finance goals",
            R.drawable.ic_finance_illustration
        ),
        OnboardingItem(
            "Control your spend and start saving",
            "BudgetPal helps you control your spending, track your expenses, and ultimately save more money.",
            R.drawable.ic_piggybank
        ),
        OnboardingItem(
            "Together we’ll reach your financial goals",
            "If you fail to plan, you plan to fail. BudgetPal will help you stay focused on tracking your spend and reach your financial goals",
            R.drawable.ic_goal
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount() = onboardingItems.size

    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            binding.textTitle.text = item.title
            binding.textDescription.text = item.description
            binding.imageOnboarding.setImageResource(item.imageResId)
        }
    }

    data class OnboardingItem(
        val title: String,
        val description: String,
        val imageResId: Int
    )
}