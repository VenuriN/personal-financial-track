package com.example.financialtraker.ui.transactions

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
) : ListAdapter<Transaction, RecentTransactionsAdapter.TransactionViewHolder>(
    TransactionDiffCallback()
) {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                tvTitle.text = transaction.category
                tvCategory.text = transaction.note
                tvDate.text = DATE_FORMAT.format(transaction.date)
                tvAmount.text = formatAmount(transaction.amount)

                // Set type indicator color
                val color = if (transaction.type == TransactionType.INCOME) {
                    ContextCompat.getColor(itemView.context, R.color.income_green)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.expense_red)
                }
                viewTypeIndicator.setBackgroundColor(color)

                root.setOnClickListener {
                    onTransactionClick(transaction)
                }
            }
        }

        private fun formatAmount(amount: Double): String {
            val format = java.text.NumberFormat.getCurrencyInstance(Locale.getDefault())
            format.currency = java.util.Currency.getInstance(dataManager.getCurrency())
            return format.format(amount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
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