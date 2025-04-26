package com.example.financialtraker.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financialtraker.R
import com.google.android.material.button.MaterialButton

class OnboardingFragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding3, container, false)
        view.findViewById<MaterialButton>(R.id.btnFinish).setOnClickListener {
            findNavController().navigate(R.id.action_onboarding3_to_passcode)
        }
        view.findViewById<MaterialButton>(R.id.btnSkip).setOnClickListener {
            findNavController().navigate(R.id.action_onboarding3_to_passcode)
        }
        return view
    }
} 