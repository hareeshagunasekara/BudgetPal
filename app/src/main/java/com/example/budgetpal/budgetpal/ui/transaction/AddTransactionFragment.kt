package com.example.budgetpal.budgetpal.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.data.Result
import com.example.budgetpal.budgetpal.data.Transaction
import com.example.budgetpal.budgetpal.databinding.ActivityAddTransactionFragmentBinding
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class AddTransactionFragment : Fragment() {
    private var _binding: ActivityAddTransactionFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddTransactionViewModel
    private lateinit var incomeAdapter: ArrayAdapter<String>
    private lateinit var expenseAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAddTransactionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceManager = PreferenceManager(requireContext())
        val factory = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddTransactionViewModel(preferenceManager) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(AddTransactionViewModel::class.java)

        setupAdapters()
        setupUI()
        observeViewModel()
    }

    private fun setupAdapters() {
        // Create adapters for income and expense categories
        val incomeCategories = resources.getStringArray(R.array.income_categories).toList()
        val expenseCategories = resources.getStringArray(R.array.expense_categories).toList()

        incomeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, incomeCategories)
        incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        expenseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, expenseCategories)
        expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun setupUI() {
        binding.apply {
            // Set default radio button selection and corresponding adapter
            radioIncome.isChecked = true
            spinnerCategory.adapter = incomeAdapter

            // Set up radio group listener
            radioGroupType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radioIncome -> {
                        spinnerCategory.adapter = incomeAdapter
                        spinnerCategory.setSelection(0)
                    }
                    R.id.radioExpense -> {
                        spinnerCategory.adapter = expenseAdapter
                        spinnerCategory.setSelection(0)
                    }
                }
            }

            buttonSave.setOnClickListener {
                saveTransaction()
            }

            buttonCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun saveTransaction() {
        binding.apply {
            val title = editTextTitle.text.toString()
            val amountText = editTextAmount.text.toString()
            val category = spinnerCategory.selectedItem?.toString() ?: ""
            val type = when (radioGroupType.checkedRadioButtonId) {
                R.id.radioIncome -> Transaction.Type.INCOME
                R.id.radioExpense -> Transaction.Type.EXPENSE
                else -> {
                    Snackbar.make(root, "Please select transaction type", Snackbar.LENGTH_LONG).show()
                    return
                }
            }

            if (title.isBlank()) {
                editTextTitle.error = "Title is required"
                return
            }

            if (amountText.isBlank()) {
                editTextAmount.error = "Amount is required"
                return
            }

            if (category.isBlank()) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    editTextAmount.error = "Amount must be greater than 0"
                    return
                }

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    date = System.currentTimeMillis()
                )
                viewModel.addTransaction(transaction)
            } catch (e: NumberFormatException) {
                editTextAmount.error = "Invalid amount"
            }
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    Snackbar.make(binding.root, result.exception.message ?: "Error saving transaction", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}