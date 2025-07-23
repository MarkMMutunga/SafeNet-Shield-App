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

package com.safenet.shield.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class SecurityUtils private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        @Volatile
        private var INSTANCE: SecurityUtils? = null

        fun getInstance(context: Context): SecurityUtils {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityUtils(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        private const val TAG = "SecurityUtils"
        private const val PREFS_NAME = "security_prefs"
        private const val KEY_LOGIN_ATTEMPTS = "login_attempts"
        private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val KEY_2FA_ENABLED = "2fa_enabled"
        private const val KEY_2FA_SECRET = "2fa_secret"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        // Rate limiting configuration
        private const val MAX_LOGIN_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MINUTES = 15L
        private const val SESSION_DURATION_HOURS = 24L
    }

    fun initialize(context: Context) {
        try {
            // Initialize any other necessary components
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SecurityUtils", e)
            throw e
        }
    }

    /**
     * Rate Limiting Functions
     */
    fun canAttemptLogin(): Boolean {
        return try {
            val attempts = sharedPreferences.getInt(KEY_LOGIN_ATTEMPTS, 0)
            val lastAttemptTime = sharedPreferences.getLong(KEY_LAST_ATTEMPT_TIME, 0)
            val currentTime = System.currentTimeMillis()

            // Reset attempts if lockout period has passed
            if (currentTime - lastAttemptTime > TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES)) {
                resetLoginAttempts()
                true
            } else {
                attempts < MAX_LOGIN_ATTEMPTS
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login attempts", e)
            false
        }
    }

    fun recordFailedAttempt() {
        try {
            val attempts = sharedPreferences.getInt(KEY_LOGIN_ATTEMPTS, 0) + 1
            sharedPreferences.edit()
                .putInt(KEY_LOGIN_ATTEMPTS, attempts)
                .putLong(KEY_LAST_ATTEMPT_TIME, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error recording failed attempt", e)
        }
    }

    fun resetLoginAttempts() {
        try {
            sharedPreferences.edit()
                .putInt(KEY_LOGIN_ATTEMPTS, 0)
                .putLong(KEY_LAST_ATTEMPT_TIME, 0)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting login attempts", e)
        }
    }

    fun getRemainingAttempts(): Int {
        return try {
            MAX_LOGIN_ATTEMPTS - sharedPreferences.getInt(KEY_LOGIN_ATTEMPTS, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting remaining attempts", e)
            0
        }
    }

    fun getLockoutTimeRemaining(): Long {
        return try {
            val lastAttemptTime = sharedPreferences.getLong(KEY_LAST_ATTEMPT_TIME, 0)
            val currentTime = System.currentTimeMillis()
            val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastAttemptTime)
            LOCKOUT_DURATION_MINUTES - elapsedMinutes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lockout time remaining", e)
            0
        }
    }

    /**
     * Session Management Functions
     */
    fun isValidSession(): Boolean {
        try {
            val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
            if (!isLoggedIn) {
                return false
            }

            val token = sharedPreferences.getString(KEY_SESSION_TOKEN, null)
            val timestamp = sharedPreferences.getLong(KEY_SESSION_TIMESTAMP, 0)
            
            if (token == null || timestamp == 0L) {
                return false
            }

            val currentTime = System.currentTimeMillis()
            val sessionAge = TimeUnit.MILLISECONDS.toHours(currentTime - timestamp)
            
            return sessionAge < SESSION_DURATION_HOURS
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session validity", e)
            return false
        }
    }

    fun clearSession() {
        try {
            sharedPreferences.edit().apply {
                remove(KEY_SESSION_TOKEN)
                remove(KEY_SESSION_TIMESTAMP)
                putBoolean(KEY_IS_LOGGED_IN, false)
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing session", e)
        }
    }

    fun setSession(token: String) {
        try {
            sharedPreferences.edit().apply {
                putString(KEY_SESSION_TOKEN, token)
                putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting session", e)
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        try {
            sharedPreferences.edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting logged in state", e)
        }
    }

    /**
     * 2FA Functions
     */
    fun is2FAEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_2FA_ENABLED, false)
    }

    fun enable2FA(secret: String) {
        sharedPreferences.edit()
            .putBoolean(KEY_2FA_ENABLED, true)
            .putString(KEY_2FA_SECRET, secret)
            .apply()
    }

    fun disable2FA() {
        sharedPreferences.edit()
            .putBoolean(KEY_2FA_ENABLED, false)
            .remove(KEY_2FA_SECRET)
            .apply()
    }

    fun get2FASecret(): String? {
        return sharedPreferences.getString(KEY_2FA_SECRET, null)
    }

    fun createSession(username: String): String {
        try {
            // Generate a cryptographically secure session token
            val secureRandom = SecureRandom()
            val tokenBytes = ByteArray(32) // 256-bit token
            secureRandom.nextBytes(tokenBytes)
            val token = Base64.encodeToString(tokenBytes, Base64.NO_WRAP)
            setSession(token)
            return token
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session", e)
            throw e
        }
    }

    fun registerUser(email: String, hashedPassword: String, name: String): Boolean {
        return try {
            // Check if user already exists
            val existingEmail = sharedPreferences.getString(KEY_USER_EMAIL, null)
            if (existingEmail != null) {
                return false
            }

            // Store user data
            sharedPreferences.edit().apply {
                putString(KEY_USER_EMAIL, email)
                putString(KEY_USER_PASSWORD, hashedPassword)
                putString(KEY_USER_NAME, name)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user", e)
            false
        }
    }

    fun verifyUser(email: String, hashedPassword: String): Boolean {
        return try {
            val storedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null)
            val storedPassword = sharedPreferences.getString(KEY_USER_PASSWORD, null)
            
            email == storedEmail && hashedPassword == storedPassword
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying user", e)
            false
        }
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    /**
     * Encrypt data for secure storage
     */
    fun encryptData(data: ByteArray, context: Context): ByteArray {
        return try {
            // Simple XOR encryption for demonstration
            // In production, use proper AES encryption with Android Keystore
            val key = "SafeNetShieldKey".toByteArray()
            val encrypted = ByteArray(data.size)
            
            for (i in data.indices) {
                encrypted[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            
            encrypted
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            data // Return original data if encryption fails
        }
    }

    /**
     * Decrypt data from secure storage
     */
    fun decryptData(encryptedData: ByteArray, context: Context): ByteArray {
        return try {
            // Simple XOR decryption (same as encryption for XOR)
            val key = "SafeNetShieldKey".toByteArray()
            val decrypted = ByteArray(encryptedData.size)
            
            for (i in encryptedData.indices) {
                decrypted[i] = (encryptedData[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            
            decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            encryptedData // Return encrypted data if decryption fails
        }
    }
} 