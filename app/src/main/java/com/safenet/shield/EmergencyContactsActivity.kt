/*
 * SafeNet Shield - Personal Safety & Security Application
 * 
 * Copyright (c) 2024 Mark Mikile Mutunga
 * Email: markmiki03@gmail.com
 * Phone: +254 707 678 643
 * 
 * All rights reserved. This software and associated documentation files (the "Software"),
 * are proprietary to Mark Mikile Mutunga. Unauthorized copying, distribution, or modification
 * of this software is strictly prohibited without explicit written permission from the author.
 * 
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a particular
 * purpose and noninfringement. In no event shall the author be liable for any claim,
 * damages or other liability, whether in an action of contract, tort or otherwise,
 * arising from, out of or in connection with the software or the use or other dealings
 * in the software.
 */
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