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

import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {
    private const val TAG = "PasswordUtils"
    private const val SALT_LENGTH = 16
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    fun hashPassword(password: String): String {
        return try {
            // Generate a random salt
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)

            // Create the PBEKeySpec
            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
            )

            // Get the SecretKeyFactory
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

            // Generate the hash
            val hash = factory.generateSecret(spec).encoded

            // Combine salt and hash
            val combined = ByteArray(salt.size + hash.size)
            System.arraycopy(salt, 0, combined, 0, salt.size)
            System.arraycopy(hash, 0, combined, salt.size, hash.size)

            // Return as Base64 string
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password: ${e.message}")
            // Fallback to a simple hash in case of error
            password.hashCode().toString()
        }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            // Decode the stored hash
            val combined = Base64.decode(storedHash, Base64.NO_WRAP)
            
            // Extract salt and hash
            val salt = ByteArray(SALT_LENGTH)
            val hash = ByteArray(combined.size - SALT_LENGTH)
            System.arraycopy(combined, 0, salt, 0, salt.size)
            System.arraycopy(combined, salt.size, hash, 0, hash.size)

            // Create the PBEKeySpec
            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
            )

            // Get the SecretKeyFactory
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

            // Generate the hash
            val testHash = factory.generateSecret(spec).encoded

            // Compare hashes
            MessageDigest.isEqual(hash, testHash)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password: ${e.message}")
            false
        }
    }
} 