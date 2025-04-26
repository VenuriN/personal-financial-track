package com.example.financialtraker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.financialtraker.data.DataManager

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataManager = DataManager(this)
        if (dataManager.isFirstTime()) {
            // Show onboarding
            startActivity(Intent(this, OnboardingHostActivity::class.java))
        } else {
            // Go to main
            startActivity(Intent(this, OnboardingHostActivity::class.java))

            //startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
} 