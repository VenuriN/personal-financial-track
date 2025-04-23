package com.example.financialtraker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.databinding.ItemTransactionBinding
import com.example.financialtraker.model.Transaction
import com.example.financialtraker.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class RecentTransactionsAdapter(
    private val onTransactionClick: (Transaction) -> Unit,
    private val dataManager: DataManager
) : ListAdapter<Transaction, RecentTransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {

                tvAmount.text = formatAmount(transaction.amount, transaction.type)
                tvCategory.text = transaction.category
                tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.date)

                // Set type indicator color
                val colorRes = if (transaction.type == TransactionType.INCOME) {
                    R.color.income_green
                } else {
                    R.color.expense_red
                }
                viewTypeIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, colorRes))

                root.setOnClickListener {
                    onTransactionClick(transaction)
                }
            }
        }

        private fun formatAmount(amount: Double, type: TransactionType): String {
            val sign = if (type == TransactionType.INCOME) "+" else "-"
            val currency = dataManager.getCurrency()
            return "$sign $currency${String.format(Locale.getDefault(), "%.2f", amount)}"
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 