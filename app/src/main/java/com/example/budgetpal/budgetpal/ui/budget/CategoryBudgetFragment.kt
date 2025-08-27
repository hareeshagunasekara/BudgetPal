package com.example.budgetpal.budgetpal.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.adapter.CategoryBudgetAdapter
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.databinding.FragmentCategoryBudgetBinding
import com.example.budgetpal.budgetpal.model.CategoryBudget
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModel
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class CategoryBudgetFragment : Fragment() {
    private var _binding: FragmentCategoryBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter
    private lateinit var currencyViewModel: CurrencyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBudgetBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        currencyViewModel = ViewModelProvider(requireActivity(), CurrencyViewModelFactory(preferenceManager))
            .get(CurrencyViewModel::class.java)
        setupCategoryBudgets()
        setupBackButton()
        observeCurrencyChanges()
        return binding.root
    }

    private fun observeCurrencyChanges() {
        currencyViewModel.currency.observe(viewLifecycleOwner) { currency ->
            updateTotalAllocatedBudget()
            categoryBudgetAdapter.updateCategories(categoryBudgetAdapter.getCategoryBudgets())
        }
    }

    private fun getCategoryExpenses(category: String): Double {
        val transactions = preferenceManager.getTransactions()
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        return transactions
            .filter { transaction ->
                val transactionDate = java.util.Calendar.getInstance().apply {
                    timeInMillis = transaction.date
                }
                transactionDate.get(java.util.Calendar.MONTH) == currentMonth &&
                        transactionDate.get(java.util.Calendar.YEAR) == currentYear &&
                        transaction.category == category
            }
            .sumOf { it.amount }
    }

    private fun setupCategoryBudgets() {
        // Get expense categories from resources
        val expenseCategories = resources.getStringArray(R.array.expense_categories)
        
        // Create CategoryBudget objects for expense categories
        val categoryBudgets = expenseCategories.map { category ->
            CategoryBudget(
                category,
                preferenceManager.getCategoryBudget(category),
                getCategoryExpenses(category)
            )
        }

        // Initialize adapter with save callback
        categoryBudgetAdapter = CategoryBudgetAdapter(
            categoryBudgets,
            { category, amount -> saveCategoryBudget(category, amount) },
            preferenceManager
        )

        // Setup RecyclerView
        binding.rvCategoryBudgets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryBudgetAdapter
        }

        // Display total allocated budget
        updateTotalAllocatedBudget()
    }

    private fun saveCategoryBudget(category: String, amount: Double) {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val currentTotalAllocated = calculateTotalAllocatedBudget() - preferenceManager.getCategoryBudget(category)
            
            if (currentTotalAllocated + amount > monthlyBudget) {
                Toast.makeText(
                    requireContext(),
                    "Total category budgets cannot exceed monthly budget of ${formatCurrency(monthlyBudget)}",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            preferenceManager.saveCategoryBudget(category, amount)
            updateTotalAllocatedBudget()
            Toast.makeText(requireContext(), "Category budget saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving category budget", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotalAllocatedBudget(): Double {
        return categoryBudgetAdapter.getCategoryBudgets().sumOf { it.budgetAmount }
    }

    private fun updateTotalAllocatedBudget() {
        val totalAllocated = calculateTotalAllocatedBudget()
        val monthlyBudget = preferenceManager.getMonthlyBudget()
        val remaining = monthlyBudget - totalAllocated

        binding.tvBudgetInfo.text = "Monthly Budget: ${formatCurrency(monthlyBudget)}\n" +
                "Allocated: ${formatCurrency(totalAllocated)}\n" +
                "Remaining: ${formatCurrency(remaining)}"
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val currency = preferenceManager.getSelectedCurrency()
            val locale = when (currency) {
                "USD" -> Locale.US
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                "INR" -> Locale("en", "IN")
                "AUD" -> Locale("en", "AU")
                "CAD" -> Locale("en", "CA")
                "LKR" -> Locale("si", "LK")
                "CNY" -> Locale("zh", "CN")
                "SGD" -> Locale("en", "SG")
                "MYR" -> Locale("ms", "MY")
                "THB" -> Locale("th", "TH")
                "IDR" -> Locale("id", "ID")
                "PHP" -> Locale("en", "PH")
                "VND" -> Locale("vi", "VN")
                "KRW" -> Locale("ko", "KR")
                "AED" -> Locale("ar", "AE")
                "SAR" -> Locale("ar", "SA")
                "QAR" -> Locale("ar", "QA")
                else -> Locale.US
            }
            val format = NumberFormat.getCurrencyInstance(locale)
            format.format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 