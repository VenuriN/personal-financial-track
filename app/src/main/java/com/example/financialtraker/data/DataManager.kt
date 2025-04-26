package com.example.financialtraker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.financialtraker.model.Transaction
import com.example.financialtraker.model.TransactionType
import com.example.financialtraker.ui.settings.SettingsFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date

class DataManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val gson = Gson()

    companion object {
        private const val PREFS_NAME = "FinanceTrackerPrefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_PASSCODE = "passcode"
        private const val KEY_FIRST_TIME = "first_time"
    }

    // Transaction operations
    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        transactions.removeIf { it.id == transactionId }
        saveTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Budget operations
    fun setMonthlyBudget(budget: Double) {
        sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }

    // Currency operations
    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun getCurrencySymbol(): String {
        val currency = getCurrency()
        return SettingsFragment.SUPPORTED_CURRENCIES.find { it.first == currency }?.second ?: "$"
    }

    // Theme operations
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    // Notification operations
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
    }

    // Passcode operations
    fun setPasscode(passcode: String) {
        sharedPreferences.edit().putString(KEY_PASSCODE, passcode).apply()
    }

    fun getPasscode(): String {
        return sharedPreferences.getString(KEY_PASSCODE, "1234") ?: "1234"
    }

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTime(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME, completed).apply()
    }

    // Helper methods
    fun getMonthlyExpenses(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        return getTransactions()
            .filter { 
                val transactionCalendar = Calendar.getInstance().apply { time = it.date }
                it.type == TransactionType.EXPENSE && transactionCalendar.get(Calendar.MONTH) == currentMonth 
            }
            .sumOf { it.amount }
    }

    fun getMonthlyIncome(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        return getTransactions()
            .filter { 
                val transactionCalendar = Calendar.getInstance().apply { time = it.date }
                it.type == TransactionType.INCOME && transactionCalendar.get(Calendar.MONTH) == currentMonth 
            }
            .sumOf { it.amount }
    }

    fun getCategoryExpenses(category: String): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        return getTransactions()
            .filter { 
                val transactionCalendar = Calendar.getInstance().apply { time = it.date }
                it.type == TransactionType.EXPENSE && 
                it.category == category && 
                transactionCalendar.get(Calendar.MONTH) == currentMonth 
            }
            .sumOf { it.amount }
    }
} 