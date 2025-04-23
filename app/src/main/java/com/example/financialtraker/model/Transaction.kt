package com.example.financialtraker.model

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Date = Date(),
    val note: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE;

    companion object {
        fun fromString(value: String): TransactionType {
            return when (value.uppercase()) {
                "INCOME" -> INCOME
                "EXPENSE" -> EXPENSE
                else -> throw IllegalArgumentException("Unknown transaction type: $value")
            }
        }
    }
} 