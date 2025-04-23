package com.example.financialtraker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.FragmentBudgetBinding
import com.example.financialtraker.model.TransactionCategories
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataManager = DataManager(requireContext())
        setupUI()
        loadBudgetData()
    }

    private fun setupUI() {
        // Load current budget
        val currentBudget = dataManager.getMonthlyBudget()
        if (currentBudget > 0) {
            binding.etBudget.setText(currentBudget.toString())
        }

        // Setup save button
        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        // Setup bar chart
        setupBarChart()
    }

    private fun loadBudgetData() {
        val budget = dataManager.getMonthlyBudget()
        val expenses = dataManager.getMonthlyExpenses()
        val currency = dataManager.getCurrency()

        if (budget > 0) {
            val progress = (expenses / budget * 100).toInt()
            binding.progressBudget.progress = progress
            binding.tvBudgetProgress.text = "Spent: ${formatCurrency(expenses, currency)} of ${formatCurrency(budget, currency)} ($progress%)"
            binding.tvBudgetRemaining.text = "Remaining: ${formatCurrency(budget - expenses, currency)}"
        }

        updateBarChart()
    }

    private fun saveBudget() {
        val budgetText = binding.etBudget.text.toString()
        if (budgetText.isBlank()) {
            showError("Please enter a budget amount")
            return
        }

        try {
            val budget = budgetText.toDouble()
            if (budget <= 0) {
                showError("Budget must be greater than 0")
                return
            }

            dataManager.setMonthlyBudget(budget)
            loadBudgetData()
            showSuccess("Budget saved successfully")
        } catch (e: NumberFormatException) {
            showError("Please enter a valid number")
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            legend.isEnabled = true
            setTouchEnabled(false)
        }
    }

    private fun updateBarChart() {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        TransactionCategories.expenseCategories.forEachIndexed { index, category ->
            val amount = dataManager.getCategoryExpenses(category)
            if (amount > 0) {
                entries.add(BarEntry(index.toFloat(), amount.toFloat()))
                labels.add(category)
                colors.add(ColorTemplate.MATERIAL_COLORS[index % ColorTemplate.MATERIAL_COLORS.size])
            }
        }

        val dataSet = BarDataSet(entries, "Expenses by Category")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = android.R.color.black

        binding.barChart.data = BarData(dataSet)
        binding.barChart.invalidate()
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = java.util.Currency.getInstance(currency)
        return format.format(amount)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 