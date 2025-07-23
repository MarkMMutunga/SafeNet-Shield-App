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
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.databinding.ActivitySafetyTipsBinding

class SafetyTipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySafetyTipsBinding
    private val TAG = "SafetyTipsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafetyTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        try {
            // Set up emergency contacts button
            binding.emergencyButton.setOnClickListener {
                startActivity(Intent(this, EmergencyContactsActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            Toast.makeText(this, "Error loading safety tips", Toast.LENGTH_SHORT).show()
        }
    }
} 