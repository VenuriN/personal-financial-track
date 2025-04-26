package com.example.financialtraker.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financialtraker.R
import com.google.android.material.button.MaterialButton

class OnboardingFragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding2, container, false)
        view.findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            findNavController().navigate(R.id.action_onboarding2_to_onboarding3)
        }
        view.findViewById<MaterialButton>(R.id.btnSkip).setOnClickListener {
            findNavController().navigate(R.id.action_onboarding2_to_passcode)
        }
        return view
    }
} 