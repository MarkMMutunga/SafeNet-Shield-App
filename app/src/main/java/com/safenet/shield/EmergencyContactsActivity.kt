package com.safenet.shield

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.databinding.ActivityEmergencyContactsBinding

class EmergencyContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmergencyContactsBinding
    private val TAG = "EmergencyContactsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        try {
            // Set up click listeners for phone numbers
            setupPhoneNumberClickListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            Toast.makeText(this, "Error loading emergency contacts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPhoneNumberClickListeners() {
        try {
            // General Emergencies
            binding.generalEmergenciesCard.setOnClickListener {
                dialPhoneNumber("999")
            }

            // Cybercrime
            binding.cybercrimeCard.setOnClickListener {
                dialPhoneNumber("+254716148341")
            }

            // Mental Health
            binding.mentalHealthCard.setOnClickListener {
                dialPhoneNumber("+254722178177")
            }

            // Child Protection
            binding.childProtectionCard.setOnClickListener {
                dialPhoneNumber("116")
            }

            // GBV Support
            binding.gbvCard.setOnClickListener {
                dialPhoneNumber("1195")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up phone number listeners", e)
        }
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error dialing phone number", e)
            Toast.makeText(this, "Error making phone call", Toast.LENGTH_SHORT).show()
        }
    }
} 