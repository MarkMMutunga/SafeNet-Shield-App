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

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {
    
    // Email validation
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Password validation - enhanced security requirements
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }
    
    // Name validation
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length >= 2 && 
               name.length <= 50 &&
               name.matches(Regex("^[a-zA-Z\\s]+$"))
    }
    
    // Phone number validation (basic)
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotBlank() && 
               phone.matches(Regex("^[+]?[0-9]{10,15}$"))
    }
    
    // Sanitize input to prevent injection attacks
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[<>\"'%;()&+]"), "") // Remove potentially dangerous characters
    }
    
    // Validate file types for uploads
    fun isValidFileType(fileName: String, allowedTypes: List<String>): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return allowedTypes.contains(extension)
    }
    
    // Check for common SQL injection patterns
    fun containsSqlInjection(input: String): Boolean {
        val sqlPatterns = listOf(
            "('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))",
            "(union|select|insert|delete|update|drop|create|alter|exec|execute)",
            "(script|javascript|vbscript|onload|onerror|onclick)"
        )
        
        val lowercaseInput = input.lowercase()
        return sqlPatterns.any { pattern ->
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(lowercaseInput).find()
        }
    }
    
    // Validate report content
    fun isValidReportContent(content: String): Boolean {
        return content.isNotBlank() && 
               content.length >= 10 && 
               content.length <= 5000 &&
               !containsSqlInjection(content)
    }
    
    fun getPasswordStrengthMessage(password: String): String {
        val issues = mutableListOf<String>()
        
        if (password.length < 8) issues.add("at least 8 characters")
        if (!password.any { it.isUpperCase() }) issues.add("uppercase letter")
        if (!password.any { it.isLowerCase() }) issues.add("lowercase letter")
        if (!password.any { it.isDigit() }) issues.add("number")
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) issues.add("special character")
        
        return if (issues.isEmpty()) {
            "Strong password"
        } else {
            "Password must include: ${issues.joinToString(", ")}"
        }
    }
}
