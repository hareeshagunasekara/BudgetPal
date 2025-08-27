package com.example.budgetpal.budgetpal.ui.budget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.adapter.CategoryBudgetAdapter
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.databinding.ActivityBudgetFragmentBinding
import com.example.budgetpal.budgetpal.model.CategoryBudget
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModel
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModelFactory
import com.example.budgetpal.budgetpal.util.NotificationUtils
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: ActivityBudgetFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: BudgetViewModel
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                "Notifications are disabled. Please enable them in settings to receive budget alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = ActivityBudgetFragmentBinding.inflate(inflater, container, false)
            preferenceManager = PreferenceManager(requireContext())
            viewModel = ViewModelProvider(
                this,
                BudgetViewModel.Factory(preferenceManager)
            )[BudgetViewModel::class.java]
            currencyViewModel = ViewModelProvider(requireActivity(), CurrencyViewModelFactory(preferenceManager))
                .get(CurrencyViewModel::class.java)

            setupUI()
            setupClickListeners()
            observeViewModel()
            observeCurrencyChanges()
            setupBarChart()
            setupCategoryBudgets()
            checkNotificationPermission()

            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error initializing budget screen", Toast.LENGTH_SHORT).show()
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSaveButton()
    }

    override fun onResume() {
        super.onResume()
        try {
            updateBudgetProgress()
            updateBarChart()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        try {
            val currentBudget = preferenceManager.getMonthlyBudget()
            binding.etMonthlyBudget.setText(currentBudget.toString())
            updateBudgetProgress()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.etMonthlyBudget.setText("0")
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }
        
        binding.btnCategoryBudgets.setOnClickListener {
            findNavController().navigate(R.id.categoryBudgetFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            try {
                binding.etMonthlyBudget.setText(budget.toString())
                updateBudgetProgress()
                updateBarChart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeCurrencyChanges() {
        currencyViewModel.currency.observe(viewLifecycleOwner) { currency ->
            updateBudgetProgress()
            updateBarChart()
            updateUI()
        }
    }

    private fun updateUI() {
        val currentBudget = preferenceManager.getMonthlyBudget()
        binding.etMonthlyBudget.setText(currentBudget.toString())
        updateBudgetProgress()
    }

    private fun saveBudget() {
        try {
            val budget = binding.etMonthlyBudget.text.toString().toDouble()
            if (budget < 0) {
                Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                return
            }

            // Calculate total allocated category budgets
            val expenseCategories = resources.getStringArray(R.array.expense_categories)
            val totalAllocated = expenseCategories.sumOf { category ->
                preferenceManager.getCategoryBudget(category)
            }

            // Check if new budget is less than allocated category budgets
            if (budget < totalAllocated) {
                Toast.makeText(
                    requireContext(),
                    "Monthly budget cannot be less than total allocated category budgets (${formatCurrency(totalAllocated)})",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            viewModel.updateBudget(budget)
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBudgetProgress() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = viewModel.getMonthlyExpenses()
            val progress = if (monthlyBudget > 0) {
                (monthlyExpenses / monthlyBudget * 100).toInt()
            } else {
                0
            }

            binding.progressBudget.progress = progress
            binding.tvBudgetStatus.text = "$progress%"

            // Trigger notifications
            if (progress >= 80 && progress < 100) {
                NotificationUtils.showBudgetNotification(
                    requireContext(),
                    "Budget Alert",
                    "You're approaching your monthly budget!"
                )
            } else if (progress >= 100) {
                NotificationUtils.showBudgetNotification(
                    requireContext(),
                    "Budget Exceeded",
                    "You've exceeded your monthly budget!"
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            binding.progressBudget.progress = 0
            binding.tvBudgetStatus.text = "0%"
        }
    }

    private fun setupBarChart() {
        try {
            binding.barChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                    textSize = 12f
                    formSize = 12f
                    xEntrySpace = 10f
                    yEntrySpace = 5f
                }

                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawBarShadow(false)
                setDrawGridBackground(false)
                
                // X-axis setup
                xAxis.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value.toInt()) {
                                0 -> "Expenses"
                                1 -> "Remaining"
                                else -> ""
                            }
                        }
                    }
                }

                // Left axis setup
                axisLeft.apply {
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    axisMinimum = 0f
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatCurrency(value.toDouble())
                        }
                    }
                }

                // Right axis setup
                axisRight.isEnabled = false

                // Animation
                animateY(1000)

                setNoDataText("No budget data available")
                setNoDataTextColor(ContextCompat.getColor(context, R.color.gray))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBarChart() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = viewModel.getMonthlyExpenses()
            val remaining = monthlyBudget - monthlyExpenses

            val entries = listOf(
                BarEntry(0f, monthlyExpenses.toFloat()),
                BarEntry(1f, remaining.toFloat())
            )

            val dataSet = BarDataSet(entries, "Budget Breakdown").apply {
                colors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.expense_color),
                    ContextCompat.getColor(requireContext(), R.color.income_color)
                )
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                valueTextSize = 12f
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
                setDrawValues(true)
            }

            binding.barChart.apply {
                data = BarData(dataSet).apply {
                    barWidth = 0.6f
                }
                
                // Update axis minimum and maximum for better visualization
                axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = (monthlyBudget * 1.1).toFloat() // Add 10% padding
                }
                
                // Add marker view for detailed information
                marker = object : com.github.mikephil.charting.components.MarkerView(
                    context,
                    R.layout.marker_view
                ) {
                    override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
                        if (e == null) return
                        val value = e.y
                        val percentage = (value / monthlyBudget * 100).toInt()
                        val label = when (e.x.toInt()) {
                            0 -> "Expenses: ${formatCurrency(value.toDouble())} ($percentage%)"
                            1 -> "Remaining: ${formatCurrency(value.toDouble())} ($percentage%)"
                            else -> ""
                        }
                        // Assuming you have a TextView with id 'tvContent' in your marker_view layout
                        findViewById<TextView>(R.id.tvContent).text = label
                    }
                }

                // Refresh the chart
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.barChart.setNoDataText("Error loading budget data")
            binding.barChart.invalidate()
        }
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

    private fun setupCategoryBudgets() {
        // Remove this function as it's now in CategoryBudgetFragment
    }

    private fun saveCategoryBudget(category: String, amount: Double) {
        // Remove this function as it's now in CategoryBudgetFragment
    }

    private fun setupSaveButton() {
        // Remove this function as it's now in CategoryBudgetFragment
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}