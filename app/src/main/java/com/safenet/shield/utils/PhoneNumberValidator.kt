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

object PhoneNumberValidator {
    // Country codes and their respective phone number lengths (excluding country code)
    private val countryPhoneRules = mapOf(
        "KE" to 9,  // Kenya: 9 digits after country code
        "UG" to 9,  // Uganda: 9 digits after country code
        "TZ" to 9,  // Tanzania: 9 digits after country code
        "RW" to 9,  // Rwanda: 9 digits after country code
        "ET" to 9,  // Ethiopia: 9 digits after country code
        "SO" to 9,  // Somalia: 9 digits after country code
        "SS" to 9,  // South Sudan: 9 digits after country code
        "SD" to 9,  // Sudan: 9 digits after country code
        "ER" to 9,  // Eritrea: 9 digits after country code
        "DJ" to 8   // Djibouti: 8 digits after country code
    )

    fun validatePhoneNumber(countryCode: String, phoneNumber: String): Boolean {
        // Remove any non-digit characters
        val cleanPhoneNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // Get the expected length for the country
        val expectedLength = countryPhoneRules[countryCode] ?: return false
        
        // Check if the phone number has the correct length
        return cleanPhoneNumber.length == expectedLength
    }

    fun getPhoneNumberLength(countryCode: String): Int {
        return countryPhoneRules[countryCode] ?: 0
    }

    fun formatPhoneNumber(countryCode: String, phoneNumber: String): String {
        val cleanPhoneNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return "+${getCountryCodePrefix(countryCode)}$cleanPhoneNumber"
    }

    private fun getCountryCodePrefix(countryCode: String): String {
        return when (countryCode) {
            "KE" -> "254"  // Kenya
            "UG" -> "256"  // Uganda
            "TZ" -> "255"  // Tanzania
            "RW" -> "250"  // Rwanda
            "ET" -> "251"  // Ethiopia
            "SO" -> "252"  // Somalia
            "SS" -> "211"  // South Sudan
            "SD" -> "249"  // Sudan
            "ER" -> "291"  // Eritrea
            "DJ" -> "253"  // Djibouti
            else -> ""
        }
    }
} 