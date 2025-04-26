package com.example.financialtraker.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentAddTransactionBinding
import com.example.financialtraker.model.Transaction
import com.example.financialtraker.model.TransactionCategories
import com.example.financialtraker.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var editingTransactionId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataManager = DataManager(requireContext())

        setupTransactionTypeToggle()
        setupCategorySpinner()
        setupDatePicker()
        setupSaveButton()
        loadTransactionData()
    }

    private fun loadTransactionData() {
        arguments?.let { bundle ->
            editingTransactionId = bundle.getString("transaction_id")
            val amount = bundle.getDouble("amount")
            val type = TransactionType.valueOf(bundle.getString("type") ?: "EXPENSE")
            val category = bundle.getString("category") ?: ""
            val date = Date(bundle.getLong("date"))
            val note = bundle.getString("note") ?: ""

            // Set the transaction type
            binding.tgTransactionType.check(
                when (type) {
                    TransactionType.INCOME -> R.id.btnIncome 
                    TransactionType.EXPENSE -> R.id.btnExpense
                }
            )

            // Set the amount
            binding.etAmount.setText(amount.toString())

            // Set the category
            updateCategories(type)
            val categoryAdapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
            val position = categoryAdapter.getPosition(category)
            if (position >= 0) {
                binding.spinnerCategory.setSelection(position)
            }

            // Set the date
            calendar.time = date
            binding.etDate.setText(dateFormat.format(date))

            // Set the note
            binding.etNote.setText(note)
        }
    }

    private fun setupTransactionTypeToggle() {
        binding.tgTransactionType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnIncome -> {
                        binding.btnIncome.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.income_green)
                        )
                        binding.btnExpense.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.light_red)
                        )
                        updateCategories(TransactionType.INCOME)
                    }
                    R.id.btnExpense -> {
                        binding.btnExpense.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.expense_red)
                        )
                        binding.btnIncome.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.light_green)
                        )
                        updateCategories(TransactionType.EXPENSE)
                    }
                }
            }
        }

        // Set initial colors and categories
        binding.btnIncome.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.income_green)
        )
        binding.btnExpense.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.light_red)
        )
        updateCategories(TransactionType.INCOME)
    }

    private fun setupCategorySpinner() {
        updateCategories(TransactionType.EXPENSE) // Default to expense categories
    }

    private fun updateCategories(type: TransactionType) {
        val categories = when (type) {
            TransactionType.INCOME -> TransactionCategories.incomeCategories
            TransactionType.EXPENSE -> TransactionCategories.expenseCategories
        }
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (type == TransactionType.INCOME) R.color.income_green else R.color.expense_red
                    )
                )
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (type == TransactionType.INCOME) R.color.income_green else R.color.expense_red
                    )
                )
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormat.format(calendar.time))
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val category = binding.spinnerCategory.selectedItem as String
        val note = binding.etNote.text.toString()
        val type = when (binding.tgTransactionType.checkedButtonId) {
            R.id.btnIncome -> TransactionType.INCOME
            R.id.btnExpense -> TransactionType.EXPENSE
            else -> return
        }

        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = editingTransactionId ?: "", // Use existing ID if editing
            amount = amount,
            type = type,
            category = category,
            date = calendar.time,
            note = note
        )

        if (editingTransactionId != null) {
            dataManager.updateTransaction(transaction)
            Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            dataManager.saveTransaction(transaction)
            Toast.makeText(context, "Transaction saved", Toast.LENGTH_SHORT).show()
        }

        // Check balance and show alert if needed
        val balance = dataManager.getMonthlyIncome() - dataManager.getMonthlyExpenses()
        if (balance < 500) {
            val currencySymbol = dataManager.getCurrencySymbol()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Low Balance Alert")
                .setMessage("Your current balance is $currencySymbol$balance. Please manage your expenses carefully.")
                .setPositiveButton("OK") { _, _ ->
                    findNavController().navigateUp()
                }
                .setCancelable(false)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 