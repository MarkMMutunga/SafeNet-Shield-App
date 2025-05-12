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