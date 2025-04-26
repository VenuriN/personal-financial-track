package com.example.financialtraker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financialtraker.data.DataManager
import com.example.financialtraker.ui.settings.SettingsFragment

class BalanceCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataManager = DataManager(context)
        val balance = dataManager.getMonthlyIncome() - dataManager.getMonthlyExpenses()
        
        if (balance < SettingsFragment.LOW_BALANCE_THRESHOLD && dataManager.areNotificationsEnabled()) {
            showLowBalanceNotification(balance, dataManager)
        }
        
        return Result.success()
    }

    private fun showLowBalanceNotification(balance: Double, dataManager: DataManager) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currencySymbol = dataManager.getCurrencySymbol()
        val title = "Low Balance Alert"
        val text = "Your current balance is $currencySymbol$balance. Please manage your expenses carefully."

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SettingsFragment.CHANNEL_ID,
                SettingsFragment.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for low balance alerts"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
            android.app.Notification.Builder(context, SettingsFragment.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        } else {
            android.app.Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .build()
        }

        notificationManager.notify(1, notification)
    }
} 