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
package com.safenet.shield.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.MainActivity
import com.safenet.shield.databinding.ActivityTwoFactorAuthBinding
import com.safenet.shield.utils.SecurityUtils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject

class TwoFactorAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTwoFactorAuthBinding
    private lateinit var securityUtils: SecurityUtils
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            try {
                val json = JSONObject(result.contents)
                val secret = json.getString("secret")
                securityUtils.enable2FA(secret)
                Toast.makeText(this, "2FA setup successful", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid QR code", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwoFactorAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityUtils = SecurityUtils.getInstance(this)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnVerify.setOnClickListener {
            verifyCode()
        }

        binding.btnScanQr.setOnClickListener {
            startQRScanner()
        }
    }

    private fun verifyCode() {
        val code = binding.etVerificationCode.text.toString()
        if (code.length != 6) {
            binding.tilVerificationCode.error = "Please enter a valid 6-digit code"
            return
        }

        // TODO: Replace with actual 2FA verification
        // This is just a placeholder for demonstration
        val isValid = true // In real app, verify against the 2FA secret

        if (isValid) {
            Toast.makeText(this, "2FA verification successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            binding.tilVerificationCode.error = "Invalid verification code"
        }
    }

    private fun startQRScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan QR Code")
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }
} 