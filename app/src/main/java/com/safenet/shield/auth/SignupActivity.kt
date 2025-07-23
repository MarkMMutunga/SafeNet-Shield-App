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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.R
import com.safenet.shield.databinding.ActivitySignupBinding
import com.safenet.shield.utils.PasswordUtils
import com.safenet.shield.utils.SecurityUtils

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        setupClickListeners()
        setupTextWatchers()
        updateSignupButtonState()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            attemptSignup()
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSignupButtonState()
            }
        }

        binding.nameEditText.addTextChangedListener(textWatcher)
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun updateSignupButtonState() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        binding.signupButton.isEnabled = name.isNotEmpty() &&
                email.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                password == confirmPassword
    }

    private fun attemptSignup() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Hash the password
        val hashedPassword = PasswordUtils.hashPassword(password)

        // Store user data
        val securityUtils = SecurityUtils.getInstance(this)
        if (securityUtils.registerUser(email, hashedPassword, name)) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Registration failed. Email may already be in use.", Toast.LENGTH_SHORT).show()
        }
    }
} 