package com.example.budgetpal.budgetpal.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    fun createBackup(preferenceManager: PreferenceManager): Result<String> {
        return try {
            // Get all data to backup
            val transactions = preferenceManager.getTransactions()
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val categories = preferenceManager.getCategories()
            val categoryBudgets = categories.associateWith { preferenceManager.getCategoryBudget(it) }
            val settings = mapOf(
                "monthly_budget" to monthlyBudget,
                "category_budgets" to categoryBudgets,
                "selected_currency" to preferenceManager.getSelectedCurrency(),
                "onboarding_completed" to preferenceManager.isOnboardingCompleted()
            )

            // Create backup data object
            val backupData = BackupData(
                transactions = transactions,
                budgets = categoryBudgets.map { (category, amount) -> 
                    Budget(category, amount)
                },
                settings = settings,
                timestamp = System.currentTimeMillis()
            )

            // Convert to JSON
            val json = gson.toJson(backupData)

            // Create backup file
            val timestamp = dateFormat.format(Date())
            val fileName = "budgetpal_backup_$timestamp.json"
            val file = File(context.filesDir, fileName)

            // Write to file
            FileOutputStream(file).use { outputStream ->
                outputStream.write(json.toByteArray())
            }

            Result.Success(fileName)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error creating backup", e)
            Result.Error(e)
        }
    }

    fun restoreBackup(fileName: String, preferenceManager: PreferenceManager): Result<Unit> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                return Result.Error(IOException("Backup file not found"))
            }

            // Read backup file
            val json = FileInputStream(file).use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }

            // Parse JSON
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(json, type)

            // Restore data
            preferenceManager.saveTransactions(backupData.transactions)
            
            // Restore budgets
            backupData.budgets.forEach { budget ->
                preferenceManager.saveCategoryBudget(budget.category, budget.amount)
            }
            
            // Restore settings
            backupData.settings["monthly_budget"]?.let { 
                preferenceManager.saveMonthlyBudget((it as Number).toDouble())
            }
            backupData.settings["selected_currency"]?.let {
                preferenceManager.setSelectedCurrency(it as String)
            }
            backupData.settings["onboarding_completed"]?.let {
                preferenceManager.setOnboardingCompleted(it as Boolean)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restoring backup", e)
            Result.Error(e)
        }
    }

    fun getBackupFiles(): List<String> {
        return context.filesDir.listFiles { file ->
            file.name.startsWith("budgetpal_backup_") && file.name.endsWith(".json")
        }?.map { it.name } ?: emptyList()
    }

    data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val settings: Map<String, Any>,
        val timestamp: Long
    )

    data class Budget(
        val category: String,
        val amount: Double
    )
} 