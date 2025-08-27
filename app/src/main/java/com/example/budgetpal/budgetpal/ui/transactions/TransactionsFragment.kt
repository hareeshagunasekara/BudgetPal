package com.example.budgetpal.budgetpal.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.Transaction
import com.example.budgetpal.budgetpal.databinding.ActivityTransactionsFragmentBinding
import com.example.budgetpal.budgetpal.data.PreferenceManager
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {

    private lateinit var binding: ActivityTransactionsFragmentBinding
    private val preferenceManager: PreferenceManager by lazy { PreferenceManager(requireContext()) }
    private val viewModel: TransactionsViewModel by viewModels {
        TransactionsViewModelFactory(preferenceManager, requireContext())
    }
    private lateinit var adapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityTransactionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (isAdded && !isDetached) {
            viewModel.loadTransactions()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionsAdapter(
            onEditClick = { transaction ->
                val action = TransactionsFragmentDirections
                    .actionNavigationTransactionsToEditTransactionFragment(transaction)
                findNavController().navigate(action)
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            },
            preferenceManager = preferenceManager
        )
        binding.recyclerViewTransactions.adapter = adapter
        binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_transactions_to_addTransactionFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}