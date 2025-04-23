package com.example.financialtraker.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentDashboardBinding
import com.example.financialtraker.model.Transaction
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private lateinit var recentTransactionsAdapter: RecentTransactionsAdapter
    private val TAG = "DashboardFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            Log.d(TAG, "Creating view")
            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d(TAG, "Setting up view")
            dataManager = DataManager(requireContext())
            setupRecentTransactions()
            setupPieChart()
            updateData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun setupRecentTransactions() {
        recentTransactionsAdapter = RecentTransactionsAdapter(
            onTransactionClick = { transaction ->
                // Handle transaction click
            },
            dataManager = dataManager
        )

        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentTransactionsAdapter
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.R.color.transparent)
            setTransparentCircleAlpha(0)
            legend.isEnabled = true
            setEntryLabelColor(android.R.color.black)
            setEntryLabelTextSize(12f)
        }
    }

    private fun updateData() {
        val income = dataManager.getMonthlyIncome()
        val expenses = dataManager.getMonthlyExpenses()
        val balance = income - expenses
        val budget = dataManager.getMonthlyBudget()
        val currency = dataManager.getCurrency()

        // Update balance card (moved to top)
        binding.tvBalance.text = formatCurrency(balance, currency)
        binding.tvBalance.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (balance >= 0) R.color.green else R.color.red
            )
        )

        // Update income and expense cards
        binding.tvIncome.text = formatCurrency(income, currency)
        binding.tvIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        
        binding.tvExpenses.text = formatCurrency(expenses, currency)
        binding.tvExpenses.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))

        // Update budget progress
        if (budget > 0) {
            val progress = (expenses / budget * 100).toInt()
            binding.progressBudget.progress = progress
            binding.tvBudgetProgress.text = "Spent: ${formatCurrency(expenses, currency)} of ${formatCurrency(budget, currency)} ($progress%)"
        }

        // Update pie chart with income/expense ratio
        updatePieChart(income, expenses)

        // Update recent transactions
        val recentTransactions = dataManager.getTransactions()
            .sortedByDescending { it.date }
            .take(5)
        recentTransactionsAdapter.submitList(recentTransactions)
    }

    private fun updatePieChart(income: Double, expenses: Double) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        val total = income + expenses

        if (total > 0) {
            if (income > 0) {
                val incomePercentage = (income / total * 100).toFloat()
                entries.add(PieEntry(incomePercentage, "Income"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.green))
            }
            if (expenses > 0) {
                val expensesPercentage = (expenses / total * 100).toFloat()
                entries.add(PieEntry(expensesPercentage, "Expenses"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.red))
            }
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        binding.pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.black))
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = java.util.Currency.getInstance(currency)
        return format.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 