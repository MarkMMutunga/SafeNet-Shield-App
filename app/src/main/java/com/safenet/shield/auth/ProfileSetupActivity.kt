package com.safenet.shield.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.MainActivity
import com.safenet.shield.databinding.ActivityProfileSetupBinding
import com.safenet.shield.utils.PhoneNumberValidator

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private var selectedImageUri: Uri? = null
    private var selectedCountryCode: String = "KE" // Default to Kenya

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePhoneNumber()
            }
        })
    }

    private fun validatePhoneNumber() {
        val phoneNumber = binding.etPhone.text.toString()
        val expectedLength = PhoneNumberValidator.getPhoneNumberLength(selectedCountryCode)
        
        if (phoneNumber.length > expectedLength) {
            binding.etPhone.setText(phoneNumber.substring(0, expectedLength))
            binding.etPhone.setSelection(expectedLength)
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            // TODO: Implement image picker
            Toast.makeText(this, "Image picker coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveProfile.setOnClickListener {
            if (validateInput()) {
                saveProfile()
            }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            return false
        }
        binding.tilFullName.error = null

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            return false
        }

        if (!PhoneNumberValidator.validatePhoneNumber(selectedCountryCode, phone)) {
            binding.tilPhone.error = "Invalid phone number length. Expected ${PhoneNumberValidator.getPhoneNumberLength(selectedCountryCode)} digits"
            return false
        }
        binding.tilPhone.error = null

        if (location.isEmpty()) {
            binding.tilLocation.error = "Location is required"
            return false
        }
        binding.tilLocation.error = null

        return true
    }

    private fun saveProfile() {
        // TODO: Implement profile saving logic
        val formattedPhone = PhoneNumberValidator.formatPhoneNumber(selectedCountryCode, binding.etPhone.text.toString())
        Toast.makeText(this, "Profile saved with phone: $formattedPhone", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
} 