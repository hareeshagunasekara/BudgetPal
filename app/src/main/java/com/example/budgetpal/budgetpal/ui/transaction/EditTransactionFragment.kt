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
import androidx.navigation.fragment.navArgs
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.data.Result
import com.example.budgetpal.budgetpal.data.Transaction
import com.example.budgetpal.budgetpal.databinding.ActivityEditTransactionFragmentBinding
import com.google.android.material.snackbar.Snackbar

class EditTransactionFragment : Fragment() {
    private var _binding: ActivityEditTransactionFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditTransactionViewModel
    private val args: EditTransactionFragmentArgs by navArgs()
    private lateinit var incomeAdapter: ArrayAdapter<String>
    private lateinit var expenseAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityEditTransactionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(this, EditTransactionViewModelFactory(preferenceManager))
            .get(EditTransactionViewModel::class.java)

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
        val transaction = args.transaction
        binding.apply {
            // Set initial values
            editTextTitle.setText(transaction.title)
            editTextAmount.setText(transaction.amount.toString())

            // Set transaction type and corresponding adapter
            if (transaction.type == Transaction.Type.INCOME) {
                radioIncome.isChecked = true
                spinnerCategory.adapter = incomeAdapter
            } else {
                radioExpense.isChecked = true
                spinnerCategory.adapter = expenseAdapter
            }

            // Set category selection
            val categories = if (transaction.type == Transaction.Type.INCOME) {
                resources.getStringArray(R.array.income_categories).toList()
            } else {
                resources.getStringArray(R.array.expense_categories).toList()
            }
            val categoryPosition = categories.indexOf(transaction.category)
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }

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

            // Set up click listeners
            buttonSave.setOnClickListener {
                updateTransaction(transaction)
            }

            buttonCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun updateTransaction(originalTransaction: Transaction) {
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

                val updatedTransaction = Transaction(
                    id = originalTransaction.id,
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    date = originalTransaction.date
                )

                viewModel.updateTransaction(updatedTransaction)
            } catch (e: NumberFormatException) {
                editTextAmount.error = "Invalid amount"
            }
        }
    }

    private fun observeViewModel() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    Snackbar.make(binding.root, result.exception.message ?: "Error updating transaction", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}