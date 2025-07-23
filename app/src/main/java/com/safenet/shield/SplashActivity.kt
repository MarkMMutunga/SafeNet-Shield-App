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