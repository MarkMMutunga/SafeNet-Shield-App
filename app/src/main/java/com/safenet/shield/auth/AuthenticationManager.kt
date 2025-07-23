package com.safenet.shield.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.safenet.shield.models.User
import com.safenet.shield.utils.PasswordUtils

object AuthenticationManager {
    private const val TAG = "AuthenticationManager"
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_USER = "user_data"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_LAST_LOGIN = "last_login"
    private const val KEY_LOGIN_ATTEMPTS = "login_attempts"
    private const val MAX_LOGIN_ATTEMPTS = 5
    private const val LOCKOUT_DURATION = 30 * 60 * 1000 // 30 minutes in milliseconds

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        try {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Removed automatic default user creation for security
            // Admin users should be created through a secure setup process
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AuthenticationManager", e)
            // Don't throw the exception, just log it
        }
    }

    private fun hasUsers(): Boolean {
        return try {
            prefs.contains(KEY_USER)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for users", e)
            false
        }
    }

    // Removed addDefaultUser() method for security - no hardcoded admin accounts

    fun authenticate(email: String, password: String): AuthResult {
        return try {
            val user = getUser()
            if (user == null) {
                return AuthResult.Error("No user found")
            }

            if (user.email != email) {
                return AuthResult.Error("Invalid email")
            }

            if (!PasswordUtils.verifyPassword(password, user.passwordHash)) {
                return AuthResult.Error("Invalid password")
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            AuthResult.Error("Authentication failed")
        }
    }

    private fun getUser(): User? {
        return try {
            val userJson = prefs.getString(KEY_USER, null)
            if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user", e)
            null
        }
    }

    fun saveUser(user: User) {
        try {
            val userJson = gson.toJson(user)
            prefs.edit().putString(KEY_USER, userJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
        }
    }

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
} 