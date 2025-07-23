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
import com.safenet.shield.MainActivity
import com.safenet.shield.R
import com.safenet.shield.databinding.ActivityLoginBinding
import com.safenet.shield.utils.ValidationUtils
import com.safenet.shield.utils.SecurityUtils

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var securityUtils: SecurityUtils
    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Initialize security components
            securityUtils = SecurityUtils.getInstance(this)
            
            // Initialize Firebase Auth
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            auth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized")
            
            // Setup UI first
            setupUI()
            
            // Initialize security components in background
            initializeSecurityComponents()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_LONG).show()
            // Don't finish here, let the user at least see the login screen
        }
    }

    private fun initializeSecurityComponents() {
        try {
            // Run in background to avoid blocking UI
            Thread {
                try {
                    AuthenticationManager.init(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing security components", e)
                    runOnUiThread {
                        Toast.makeText(this, "Error initializing security components", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting security initialization thread", e)
        }
    }

    private fun setupUI() {
        try {
            // Setup click listeners first
            setupClickListeners()
            setupTextWatchers()
            updateLoginButtonState()

            // Check session after UI is ready
            if (securityUtils.isValidSession()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            Toast.makeText(this, "Error setting up UI", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.signupLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonState()
            }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun updateLoginButtonState() {
        try {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            binding.loginButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating login button state", e)
        }
    }

    private fun validateInput(): Boolean {
        try {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Enhanced email validation
            if (email.isEmpty()) {
                binding.emailInput.error = "Email is required"
                return false
            }

            if (!ValidationUtils.isValidEmail(email)) {
                binding.emailInput.error = "Please enter a valid email address"
                return false
            }

            // Enhanced password validation
            if (password.isEmpty()) {
                binding.passwordInput.error = "Password is required"
                return false
            }

            // Sanitize inputs to prevent injection attacks
            val sanitizedEmail = ValidationUtils.sanitizeInput(email)
            val sanitizedPassword = ValidationUtils.sanitizeInput(password)

            if (ValidationUtils.containsSqlInjection(sanitizedEmail) || 
                ValidationUtils.containsSqlInjection(sanitizedPassword)) {
                Toast.makeText(this, "Invalid input detected", Toast.LENGTH_SHORT).show()
                return false
            }

            // Clear any previous errors
            binding.emailInput.error = null
            binding.passwordInput.error = null

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating input", e)
            return false
        }
    }

    private fun attemptLogin() {
        if (!validateInput()) {
            return
        }

        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        try {
            Log.d(TAG, "Attempting to login user: $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        securityUtils.createSession(email)
                        securityUtils.resetLoginAttempts()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        val errorMessage = task.exception?.message ?: "Unknown error occurred"
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Login failed: $errorMessage",
                            Toast.LENGTH_LONG).show()
                        securityUtils.recordFailedAttempt()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during login", e)
            Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
} 
