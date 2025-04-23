package com.example.financialtraker.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentSettingsBinding
import com.example.financialtraker.model.Transaction
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

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private val gson = Gson()

    companion object {
        private const val REQUEST_CODE_BACKUP = 1001
        private const val REQUEST_CODE_RESTORE = 1002
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

        setupDarkModeSwitch()
        setupCurrencyDropdown()
        setupBackupButton()
        setupRestoreButton()
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
