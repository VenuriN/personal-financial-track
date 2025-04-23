package com.example.financialtraker.model

object TransactionCategories {
    // Income Categories
    const val SALARY = "Salary"
    const val SELLING = "Selling"
    const val GIFT_INCOME = "Gift"
    const val STOCK = "Stock"
    const val OTHER_INCOME = "Other"

    // Expense Categories
    const val FOOD = "Food"
    const val FUEL = "Fuel"
    const val ENTERTAINMENT = "Entertainment"
    const val CLOTHES = "Clothes"
    const val KIDS = "Kids"
    const val GIFT_EXPENSE = "Gift"
    const val HOLIDAYS = "Holidays"
    const val SHOPPING = "Shopping"
    const val TRAVEL = "Travel"
    const val OTHER_EXPENSE = "Other"

    val incomeCategories = listOf(SALARY, SELLING, GIFT_INCOME, STOCK, OTHER_INCOME)
    val expenseCategories = listOf(FOOD, FUEL, ENTERTAINMENT, CLOTHES, KIDS, GIFT_EXPENSE, HOLIDAYS, SHOPPING, TRAVEL, OTHER_EXPENSE)

    val allCategories = incomeCategories + expenseCategories

    fun getCategoriesForType(type: TransactionType): List<String> {
        return when (type) {
            TransactionType.INCOME -> incomeCategories
            TransactionType.EXPENSE -> expenseCategories
        }
    }
} 