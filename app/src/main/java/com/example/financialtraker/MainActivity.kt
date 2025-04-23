package com.example.financialtraker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.financialtraker.data.DataManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var dataManager: DataManager
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "Starting onCreate")
            setContentView(R.layout.activity_main)

            dataManager = DataManager(this)
            Log.d(TAG, "DataManager initialized")
            
            // Set theme based on user preference
            if (dataManager.isDarkMode()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Log.d(TAG, "Theme set")

            // Setup navigation
            try {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController

                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNavigationView.setupWithNavController(navController)

                Log.d(TAG, "Navigation setup complete")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up navigation", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
}