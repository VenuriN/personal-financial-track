package com.example.financialtraker.ui.settings

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentSettingsBinding
import com.example.financialtraker.model.Transaction
import com.example.financialtraker.workers.BalanceCheckWorker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private val gson = Gson()

    companion object {
        private const val REQUEST_CODE_BACKUP = 1001
        private const val REQUEST_CODE_RESTORE = 1002
        const val CHANNEL_ID = "balance_alerts"
        const val CHANNEL_NAME = "Balance Alerts"
        const val LOW_BALANCE_THRESHOLD = 500.0
        val SUPPORTED_CURRENCIES = listOf(
            "LKR" to "Rs.",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "JPY" to "¥",
            "AUD" to "A$",
            "CAD" to "C$"
        )
    }

    private val backupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        uri?.let { backupData(it) }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { restoreData(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataManager = DataManager(requireContext())

        createNotificationChannel()
        setupDarkModeSwitch()
        setupCurrencyDropdown()
        setupBackupButton()
        setupRestoreButton()
        setupNotificationSwitch()
        checkLowBalance()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for low balance alerts"
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkLowBalance() {
        val balance = dataManager.getMonthlyIncome() - dataManager.getMonthlyExpenses()
        if (balance < LOW_BALANCE_THRESHOLD && dataManager.areNotificationsEnabled()) {
            showLowBalanceNotification(balance)
        }
    }

    private fun showLowBalanceNotification(balance: Double) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currencySymbol = dataManager.getCurrencySymbol()
        
        val notification = android.app.Notification.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Low Balance Alert")
            .setContentText("Your current balance is $currencySymbol$balance. Please manage your expenses carefully.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.isChecked = dataManager.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            dataManager.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            requireActivity().recreate() // Force activity recreation to apply theme
        }
    }

    private fun setupCurrencyDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_currency_dropdown,
            SUPPORTED_CURRENCIES.map { "${it.first} (${it.second})" }
        )
        adapter.setDropDownViewResource(R.layout.item_currency_dropdown)
        binding.actvCurrency.setAdapter(adapter)

        // Set current currency
        val currentCurrency = dataManager.getCurrency()
        val position = SUPPORTED_CURRENCIES.indexOfFirst { it.first == currentCurrency }
        if (position >= 0) {
            binding.actvCurrency.setText(adapter.getItem(position), false)
        }

        binding.actvCurrency.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = SUPPORTED_CURRENCIES[position].first
            dataManager.setCurrency(selectedCurrency)
            Snackbar.make(binding.root, "Currency updated to $selectedCurrency", Snackbar.LENGTH_SHORT).show()
        }

        // Make sure the dropdown is clickable
        binding.actvCurrency.setOnClickListener {
            binding.actvCurrency.showDropDown()
        }

        binding.tilCurrency.setEndIconOnClickListener {
            binding.actvCurrency.showDropDown()
        }
    }

    private fun setupBackupButton() {
        binding.btnBackupData.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "financial_tracker_backup_$timestamp.json"
            backupLauncher.launch(fileName)
        }
    }

    private fun setupRestoreButton() {
        binding.btnRestoreData.setOnClickListener {
            restoreLauncher.launch(arrayOf("application/json"))
        }
    }

    private fun setupNotificationSwitch() {
        binding.switchNotifications.isChecked = dataManager.areNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            dataManager.setNotificationsEnabled(isChecked)
            if (isChecked) {
                // Request notification permission if needed
                requestNotificationPermission()
                // Schedule periodic balance check
                scheduleBalanceCheck()
            } else {
                // Disable notifications
                disableNotifications()
                // Cancel periodic balance check
                cancelBalanceCheck()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // If permission is denied, turn off the switch
            binding.switchNotifications.isChecked = false
            dataManager.setNotificationsEnabled(false)
            showError("Notification permission is required for notifications")
        }
    }

    private fun disableNotifications() {
        // Cancel any existing notifications
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun scheduleBalanceCheck() {
        val balanceCheckRequest = PeriodicWorkRequestBuilder<BalanceCheckWorker>(
            15, TimeUnit.MINUTES, // Check every 15 minutes
            5, TimeUnit.MINUTES   // Flexible window of 5 minutes
        ).build()

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "balance_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                balanceCheckRequest
            )
    }

    private fun cancelBalanceCheck() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork("balance_check")
    }

    private fun backupData(uri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                val transactions = dataManager.getTransactions()
                val json = dataManager.gson.toJson(transactions)
                outputStream.write(json.toByteArray())
                showMessage("Backup created successfully")
            }
        } catch (e: Exception) {
            showError("Failed to create backup: ${e.message}")
        }
    }

    private fun restoreData(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val transactions = dataManager.gson.fromJson<List<Transaction>>(
                    json,
                    object : TypeToken<List<Transaction>>() {}.type
                )
                dataManager.saveTransactions(transactions)
                showMessage("Data restored successfully")
            }
        } catch (e: Exception) {
            showError("Failed to restore data: ${e.message}")
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
