package com.example.budgetpal.budgetpal.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.data.Transaction
import com.example.budgetpal.budgetpal.databinding.ActivityDashboardFragmentBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: ActivityDashboardFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private lateinit var legendAdapter: ChartLegendAdapter
    private lateinit var chartLegendAdapter: ChartLegendAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = ActivityDashboardFragmentBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onCreateView: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViewModel()
            setupUI()
            observeViewModel()
            setupChartLegend()
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onViewCreated: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupViewModel() {
        preferenceManager = PreferenceManager(requireContext())
        val factory = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(preferenceManager) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }

    private fun setupUI() {
        setupPieChart()
        setupLineCharts()
        legendAdapter = ChartLegendAdapter()
        binding.legendRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = legendAdapter
        }
    }

    private fun observeViewModel() {
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.loadDashboardData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            try {
                binding.tvTotalBalance.text = formatCurrency(balance ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalBalance.text = formatCurrency(0.0)
            }
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            try {
                binding.tvTotalIncome.text = formatCurrency(income ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalIncome.text = formatCurrency(0.0)
            }
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            try {
                binding.tvTotalExpense.text = formatCurrency(expense ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalExpense.text = formatCurrency(0.0)
            }
        }

        viewModel.categorySpending.observe(viewLifecycleOwner) { spending ->
            try {
                updatePieChart(spending ?: emptyMap())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            try {
                updateLineCharts(transactions ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupPieChart() {
        try {
            binding.pieChart.apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.TRANSPARENT)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setTransparentCircleColor(Color.WHITE)
                setTransparentCircleAlpha(110)

                // Center text
                centerText = "Total\nExpenses"
                setCenterTextSize(14f)
                setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

                // Disable the built-in legend as we'll use custom legend
                legend.isEnabled = false

                // Entry labels
                setDrawEntryLabels(false)
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)

                // Disable rotation
                isRotationEnabled = false
                
                // Animation
                animateY(1000, Easing.EaseInOutQuad)

                // Customize the hole
                setDrawCenterText(true)
                setHoleColor(Color.WHITE)

                // No data text
                setNoDataText("No transactions yet")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupLineCharts() {
        try {
            val commonSetup: com.github.mikephil.charting.charts.LineChart.() -> Unit = {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return try {
                            dateFormat.format(Date(value.toLong()))
                        } catch (e: Exception) {
                            ""
                        }
                    }
                }
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                axisRight.isEnabled = false
                setNoDataText("No data available")
            }

            binding.incomeChart.apply(commonSetup)
            binding.expenseChart.apply(commonSetup)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupChartLegend() {
        chartLegendAdapter = ChartLegendAdapter()
        binding.legendRecyclerView.apply {
            adapter = chartLegendAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun updatePieChart(spending: Map<String, Double>) {
        try {
            if (spending.isEmpty()) {
                binding.pieChart.setNoDataText("No transactions yet")
                binding.pieChart.invalidate()
                return
            }

            // Calculate total spending
            val totalSpending = spending.values.sum()

            // Create entries and prepare legend items
            val entries = mutableListOf<PieEntry>()
            val colors = mutableListOf<Int>()
            
            spending.forEach { (category, amount) ->
                val percentage = (amount / totalSpending * 100).toFloat()
                if (percentage > 0) {
                    entries.add(PieEntry(percentage, category))
                    
                    // Set color based on category
                    val color = when {
                        category.startsWith("Income:") -> {
                            when (category.substringAfter("Income:")) {
                                "Salary" -> Color.rgb(0, 102, 204)
                                "Investment" -> Color.rgb(51, 153, 255)
                                "Business" -> Color.rgb(102, 178, 255)
                                else -> Color.rgb(0, 102, 204)
                            }
                        }
                        else -> {
                            when (category) {
                                "Food" -> Color.rgb(255, 87, 34)
                                "Transportation" -> Color.rgb(255, 152, 0)
                                "Housing" -> Color.rgb(156, 39, 176)
                                "Utilities" -> Color.rgb(3, 169, 244)
                                "Entertainment" -> Color.rgb(139, 195, 74)
                                "Shopping" -> Color.rgb(233, 30, 99)
                                "Healthcare" -> Color.rgb(0, 150, 136)
                                "Education" -> Color.rgb(63, 81, 181)
                                "Gifts" -> Color.rgb(121, 85, 72)
                                else -> Color.rgb(158, 158, 158)
                            }
                        }
                    }
                    colors.add(color)
                }
            }

            // Create dataset
            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                valueTextSize = 14f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(binding.pieChart)
                valueLineColor = Color.WHITE
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.4f
            }

            // Update chart
            binding.pieChart.apply {
                data = PieData(dataSet)
                highlightValues(null)
                invalidate()
            }

            // Update legend items
            val legendItems = entries.mapIndexed { index, entry ->
                ChartLegendAdapter.LegendItem(
                    color = colors[index],
                    name = entry.label ?: "",
                    percentage = entry.value
                )
            }
            chartLegendAdapter.updateItems(legendItems)

        } catch (e: Exception) {
            e.printStackTrace()
            binding.pieChart.setNoDataText("Error loading data")
            binding.pieChart.invalidate()
        }
    }

    private fun updateLineCharts(transactions: List<Transaction>) {
        try {
            // Group transactions by date and type
            val incomeEntries = transactions
                .filter { it.type == Transaction.Type.INCOME }
                .groupBy { it.date }
                .map { (date, transactions) ->
                    Entry(
                        date.toFloat(),
                        transactions.sumOf { it.amount }.toFloat()
                    )
                }
                .sortedBy { it.x }

            val expenseEntries = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .groupBy { it.date }
                .map { (date, transactions) ->
                    Entry(
                        date.toFloat(),
                        transactions.sumOf { it.amount }.toFloat()
                    )
                }
                .sortedBy { it.x }

            // Update Income Chart
            if (incomeEntries.isNotEmpty()) {
                val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.green_500)
                    setDrawCircles(true)
                    setDrawValues(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatCurrency(value.toDouble())
                        }
                    }
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(ContextCompat.getColor(requireContext(), R.color.green_500))
                    setDrawFilled(true)
                    fillColor = ContextCompat.getColor(requireContext(), R.color.green_500)
                    fillAlpha = 50
                }

                binding.incomeChart.apply {
                    data = LineData(incomeDataSet)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setDrawGridBackground(false)
                    setDrawBorders(false)
                    setBorderWidth(0f)
                    setDrawMarkers(true)
                    animateY(1000, Easing.EaseInOutQuad)
                    xAxis.apply {
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return try {
                                    dateFormat.format(Date(value.toLong()))
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                        }
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        granularity = 1f
                    }
                    axisLeft.apply {
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return formatCurrency(value.toDouble())
                            }
                        }
                        setDrawGridLines(true)
                        gridColor = Color.LTGRAY
                        gridLineWidth = 0.5f
                        axisMinimum = 0f
                    }
                    axisRight.isEnabled = false
                }
            } else {
                binding.incomeChart.apply {
                    data = null
                    setNoDataText("No income transactions yet")
                    setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                }
            }
            binding.incomeChart.invalidate()

            // Update Expense Chart
            if (expenseEntries.isNotEmpty()) {
                val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.red_500)
                    setDrawCircles(true)
                    setDrawValues(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatCurrency(value.toDouble())
                        }
                    }
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(ContextCompat.getColor(requireContext(), R.color.red_500))
                    setDrawFilled(true)
                    fillColor = ContextCompat.getColor(requireContext(), R.color.red_500)
                    fillAlpha = 50
                }

                binding.expenseChart.apply {
                    data = LineData(expenseDataSet)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setDrawGridBackground(false)
                    setDrawBorders(false)
                    setBorderWidth(0f)
                    setDrawMarkers(true)
                    animateY(1000, Easing.EaseInOutQuad)
                    xAxis.apply {
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return try {
                                    dateFormat.format(Date(value.toLong()))
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                        }
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        granularity = 1f
                    }
                    axisLeft.apply {
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return formatCurrency(value.toDouble())
                            }
                        }
                        setDrawGridLines(true)
                        gridColor = Color.LTGRAY
                        gridLineWidth = 0.5f
                        axisMinimum = 0f
                    }
                    axisRight.isEnabled = false
                }
            } else {
                binding.expenseChart.apply {
                    data = null
                    setNoDataText("No expense transactions yet")
                    setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                }
            }
            binding.expenseChart.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.incomeChart.setNoDataText("Error loading data")
            binding.expenseChart.setNoDataText("Error loading data")
            binding.incomeChart.invalidate()
            binding.expenseChart.invalidate()
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
}