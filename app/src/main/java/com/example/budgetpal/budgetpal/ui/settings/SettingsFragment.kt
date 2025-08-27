package com.example.budgetpal.budgetpal.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.databinding.ActivitySettingsFragmentBinding
import com.example.budgetpal.budgetpal.data.AuthenticationManager
import com.example.budgetpal.budgetpal.data.PreferenceManager
import com.example.budgetpal.budgetpal.data.Result
import com.example.budgetpal.budgetpal.ui.auth.LoginActivity
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModel
import com.example.budgetpal.budgetpal.ui.shared.CurrencyViewModelFactory
import com.example.budgetpal.budgetpal.data.BackupManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class SettingsFragment : Fragment() {
    private var _binding: ActivitySettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var authManager: AuthenticationManager
    private lateinit var backupManager: BackupManager
    private lateinit var currencyAdapter: ArrayAdapter<String>
    private val currencies = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingsFragmentBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        currencyViewModel = ViewModelProvider(requireActivity(), CurrencyViewModelFactory(preferenceManager))
            .get(CurrencyViewModel::class.java)
        backupManager = BackupManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthenticationManager(requireContext())
        setupCurrencyAdapter()
        setupUI()
        setupClickListeners()
        setupUserInfo()
        setupLogoutButton()

        // Set initial dark mode state
        binding.switchDarkMode.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun setupCurrencyAdapter() {
        currencies.addAll(listOf(
            getString(R.string.currency_usd),
            getString(R.string.currency_eur),
            getString(R.string.currency_gbp),
            getString(R.string.currency_jpy),
            getString(R.string.currency_inr),
            getString(R.string.currency_aud),
            getString(R.string.currency_cad),
            getString(R.string.currency_lkr),
            getString(R.string.currency_cny),
            getString(R.string.currency_sgd),
            getString(R.string.currency_myr),
            getString(R.string.currency_thb),
            getString(R.string.currency_idr),
            getString(R.string.currency_php),
            getString(R.string.currency_vnd),
            getString(R.string.currency_krw),
            getString(R.string.currency_aed),
            getString(R.string.currency_sar),
            getString(R.string.currency_qar)
        ))

        currencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            currencies
        )
    }

    private fun setupUI() {
        binding.spinnerCurrency.apply {
            setAdapter(currencyAdapter)
            threshold = 1

            // Set current currency
            val currentCurrency = preferenceManager.getSelectedCurrency()
            val currencyIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }
            if (currencyIndex != -1) {
                setText(currencies[currencyIndex], false)
            }

            setOnItemClickListener { _, _, position, _ ->
                val selectedCurrency = currencyAdapter.getItem(position).toString()
                val currencyCode = selectedCurrency.substring(0, 3)
                currencyViewModel.updateCurrency(currencyCode)
                Toast.makeText(requireContext(), "Currency updated to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }

            // Handle focus changes
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDropDown()
                }
            }
        }

        binding.btnBackup.setOnClickListener {
            createBackup()
        }

        binding.btnRestore.setOnClickListener {
            showRestoreDialog()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSaveCurrency.setOnClickListener {
                val selectedCurrency = spinnerCurrency.text.toString()
                if (selectedCurrency.isNotEmpty()) {
                    val currencyCode = selectedCurrency.substring(0, 3)
                    currencyViewModel.updateCurrency(currencyCode)
                    Toast.makeText(requireContext(), "Currency saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please select a currency", Toast.LENGTH_SHORT).show()
                }
            }

            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                val mode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    private fun setupUserInfo() {
        val currentUser = authManager.getCurrentUser()
        binding.textUserEmail.text = currentUser?.email ?: "Not logged in"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    authManager.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun createBackup() {
        when (val result = backupManager.createBackup(preferenceManager)) {
            is Result.Success -> {
                val fileName = result.data
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Backup Created")
                    .setMessage("Backup saved as: $fileName")
                    .setPositiveButton("OK", null)
                    .show()
            }
            is Result.Error -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Backup Failed")
                    .setMessage(result.exception.message ?: "Unknown error occurred")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showRestoreDialog() {
        val backupFiles = backupManager.getBackupFiles()
        if (backupFiles.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("No Backups Found")
                .setMessage("No backup files found to restore from.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Backup to Restore")
            .setItems(backupFiles.toTypedArray()) { _, which ->
                restoreBackup(backupFiles[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restoreBackup(fileName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Restore")
            .setMessage("This will replace all current data with the backup data. Continue?")
            .setPositiveButton("Restore") { _, _ ->
                when (val result = backupManager.restoreBackup(fileName, preferenceManager)) {
                    is Result.Success -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Restore Successful")
                            .setMessage("Data has been restored successfully.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    is Result.Error -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Restore Failed")
                            .setMessage(result.exception.message ?: "Unknown error occurred")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh currency selection when fragment is resumed
        val currentCurrency = preferenceManager.getSelectedCurrency()
        val currencyIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }
        if (currencyIndex != -1) {
            binding.spinnerCurrency.setText(currencies[currencyIndex], false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}