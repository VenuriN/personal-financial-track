package com.example.financialtraker.ui.passcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financialtraker.R
import com.example.financialtraker.data.DataManager

class PasscodeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_passcode, container, false)
        val etPasscode = view.findViewById<EditText>(R.id.etPasscode)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            val passcode = etPasscode.text.toString()
            if (passcode == "1234") {
                // Set onboarding complete
                DataManager(requireContext()).setFirstTime(false)
                // Navigate to MainActivity
                val intent = android.content.Intent(requireContext(), com.example.financialtraker.MainActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Incorrect passcode. Try 1234.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
} 