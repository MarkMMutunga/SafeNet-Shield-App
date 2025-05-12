package com.safenet.shield.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.safenet.shield.MainActivity
import com.safenet.shield.databinding.ActivityRegisterBinding
import com.safenet.shield.utils.PhoneNumberValidator

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized")
            }

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized")

            // Setup UI
            binding = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupClickListeners()
            setupTextWatchers()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateRegisterButtonState()
            }
        }

        binding.firstNameEditText.addTextChangedListener(textWatcher)
        binding.lastNameEditText.addTextChangedListener(textWatcher)
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun updateRegisterButtonState() {
        try {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            
            binding.registerButton.isEnabled = firstName.isNotEmpty() && 
                lastName.isNotEmpty() && 
                email.isNotEmpty() && 
                password.isNotEmpty() && 
                confirmPassword.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating register button state", e)
        }
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            if (validateInput()) {
                attemptRegistration()
            }
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(): Boolean {
        try {
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (firstName.isEmpty()) {
                binding.firstNameInput.error = "First name is required"
                return false
            }
            binding.firstNameInput.error = null

            if (lastName.isEmpty()) {
                binding.lastNameInput.error = "Last name is required"
                return false
            }
            binding.lastNameInput.error = null

            if (email.isEmpty()) {
                binding.emailInput.error = "Email is required"
                return false
            }
            binding.emailInput.error = null

            if (password.isEmpty()) {
                binding.passwordInput.error = "Password is required"
                return false
            }
            binding.passwordInput.error = null

            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordInput.error = "Please confirm your password"
                return false
            }
            binding.confirmPasswordInput.error = null

            if (password != confirmPassword) {
                binding.confirmPasswordInput.error = "Passwords do not match"
                return false
            }
            binding.confirmPasswordInput.error = null

            if (password.length < 6) {
                binding.passwordInput.error = "Password must be at least 6 characters"
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in validateInput: ${e.message}", e)
            Toast.makeText(this, "Error validating input: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun attemptRegistration() {
        try {
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            Log.d(TAG, "Attempting to register user: $email")
            
            // Show loading state
            binding.registerButton.isEnabled = false
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    try {
                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            val errorMessage = task.exception?.message ?: "Unknown error occurred"
                            Log.w(TAG, "createUserWithEmail:failure: $errorMessage", task.exception)
                            Toast.makeText(baseContext, "Registration failed: $errorMessage",
                                Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in registration callback: ${e.message}", e)
                        Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        // Reset button state
                        binding.registerButton.isEnabled = true
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during registration: ${e.message}", e)
            Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            binding.registerButton.isEnabled = true
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Get the user's full name
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val fullName = "$firstName $lastName".trim()

            // Create a user profile change request
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            // Update the user's profile
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Log.w(TAG, "Failed to update user profile", task.exception)
                        Toast.makeText(this, "Registration successful, but failed to set name", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
        }
    }
} 