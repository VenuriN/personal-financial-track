package com.example.financialtraker.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentTransactionsBinding
import com.example.financialtraker.model.Transaction
import com.example.financialtraker.model.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private lateinit var adapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataManager = DataManager(requireContext())
        adapter = TransactionsAdapter(
            onTransactionClick = { transaction ->
                showTransactionOptions(transaction)
            },
            dataManager = dataManager
        )

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransactionsFragment.adapter
        }

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_transactionsFragment_to_addTransactionFragment)
        }

        loadTransactions()
    }

    private fun loadTransactions() {
        val transactions = dataManager.getTransactions()
        adapter.submitList(transactions)
    }

    private fun showTransactionOptions(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Amount: ${formatAmount(transaction.amount, transaction.type)}\n" +
                    "Category: ${transaction.category}\n" +
                    "Note: ${transaction.note}\n" +
                    "Date: ${formatDate(transaction.date)}")
            .setPositiveButton("Edit") { _, _ ->
                navigateToEditTransaction(transaction)
            }
            .setNegativeButton("Delete") { _, _ ->
                showDeleteConfirmation(transaction)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun navigateToEditTransaction(transaction: Transaction) {
        val bundle = Bundle().apply {
            putString("transaction_id", transaction.id)
            putDouble("amount", transaction.amount)
            putString("type", transaction.type.name)
            putString("category", transaction.category)
            putLong("date", transaction.date.time)
            putString("note", transaction.note)
        }
        findNavController().navigate(R.id.action_transactionsFragment_to_addTransactionFragment, bundle)
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                dataManager.deleteTransaction(transaction.id)
                loadTransactions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatAmount(amount: Double, type: TransactionType): String {
        val sign = if (type == TransactionType.INCOME) "+" else "-"
        val currency = dataManager.getCurrency()
        return "$sign $currency${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    private fun formatDate(date: java.util.Date): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 