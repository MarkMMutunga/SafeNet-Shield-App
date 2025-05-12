package com.safenet.shield

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.auth.LoginActivity
import com.safenet.shield.utils.SecurityUtils

class SplashActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Set fullscreen flags
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "Splash screen started")

            // Initialize security components
            val securityUtils = SecurityUtils.getInstance(applicationContext)
            Log.d(TAG, "Security components initialized")

            // Delay for splash screen
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNextScreen(securityUtils)
            }, SPLASH_DELAY)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            navigateToLogin()
        }
    }

    private fun navigateToNextScreen(securityUtils: SecurityUtils) {
        try {
            // Check if user is already logged in
            val isValidSession = securityUtils.isValidSession()
            Log.d(TAG, "Session validity check: $isValidSession")

            if (isValidSession) {
                Log.d(TAG, "Valid session found, navigating to MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Log.d(TAG, "No valid session, navigating to LoginActivity")
                navigateToLogin()
            }
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during navigation", e)
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Disable back button during splash screen
    }
} 