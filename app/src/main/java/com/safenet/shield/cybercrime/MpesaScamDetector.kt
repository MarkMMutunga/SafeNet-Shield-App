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

package com.safenet.shield.cybercrime

import android.content.Context
import android.util.Log
import java.util.regex.Pattern

/**
 * M-Pesa and Mobile Money Scam Detection System
 * Specialized for Kenyan mobile money fraud patterns
 */
class MpesaScamDetector(private val context: Context) {

    companion object {
        private const val TAG = "MpesaScamDetector"
        
        // Common M-Pesa scam patterns
        private val FAKE_MPESA_PATTERNS = listOf(
            "you have received.*ksh.*from.*confirm",
            "mpesa.*reversal.*confirm.*pin",
            "congratulations.*won.*lottery.*mpesa",
            "safaricom.*promotion.*send.*pin",
            "your account.*suspended.*verify.*pin",
            "urgent.*mpesa.*transaction.*failed",
            "claim.*prize.*send.*float"
        )
        
        private val SUSPICIOUS_PHONE_PATTERNS = listOf(
            "0(?!7[0-9]{8})[0-9]{9}", // Non-Kenyan mobile format
            "\\+(?!254)[0-9]+", // Non-Kenyan country code
            "07[0-9]{8}" // Check against known scammer numbers (simplified)
        )
        
        private val URGENCY_KEYWORDS = listOf(
            "urgent", "immediately", "asap", "expire", "limited time",
            "act now", "final notice", "last chance", "hurry"
        )
        
        private val SUSPICIOUS_REQUESTS = listOf(
            "send pin", "share pin", "give pin", "tell pin",
            "send password", "confirm with pin", "verify pin",
            "send float", "send money first", "pay fee"
        )
    }

    data class ScamAnalysis(
        val isLikelyScam: Boolean,
        val confidenceLevel: Float,
        val detectedPatterns: List<String>,
        val riskFactors: List<RiskFactor>,
        val recommendations: List<String>
    )

    data class RiskFactor(
        val type: RiskType,
        val description: String,
        val severity: Severity
    )

    enum class RiskType {
        FAKE_MPESA_MESSAGE,
        SUSPICIOUS_PHONE_NUMBER,
        PIN_REQUEST,
        URGENCY_TACTICS,
        LOTTERY_SCAM,
        REVERSAL_SCAM,
        UNKNOWN_SENDER
    }

    enum class Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Analyze SMS message for M-Pesa scam patterns
     */
    fun analyzeSmsMessage(
        messageBody: String,
        senderNumber: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): ScamAnalysis {
        
        val detectedPatterns = mutableListOf<String>()
        val riskFactors = mutableListOf<RiskFactor>()
        val recommendations = mutableListOf<String>()
        
        val messageText = messageBody.lowercase()
        
        // Check for fake M-Pesa message patterns
        FAKE_MPESA_PATTERNS.forEach { pattern ->
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(messageText).find()) {
                detectedPatterns.add("Fake M-Pesa message pattern")
                riskFactors.add(
                    RiskFactor(
                        RiskType.FAKE_MPESA_MESSAGE,
                        "Message mimics official M-Pesa format but contains suspicious elements",
                        Severity.HIGH
                    )
                )
            }
        }
        
        // Check sender number
        senderNumber?.let { number ->
            if (number != "MPESA" && messageText.contains("mpesa")) {
                riskFactors.add(
                    RiskFactor(
                        RiskType.UNKNOWN_SENDER,
                        "M-Pesa message from unofficial sender: $number",
                        Severity.HIGH
                    )
                )
            }
            
            SUSPICIOUS_PHONE_PATTERNS.forEach { pattern ->
                if (Pattern.compile(pattern).matcher(number).find()) {
                    riskFactors.add(
                        RiskFactor(
                            RiskType.SUSPICIOUS_PHONE_NUMBER,
                            "Suspicious phone number format",
                            Severity.MEDIUM
                        )
                    )
                }
            }
        }
        
        // Check for PIN requests
        SUSPICIOUS_REQUESTS.forEach { request ->
            if (messageText.contains(request)) {
                riskFactors.add(
                    RiskFactor(
                        RiskType.PIN_REQUEST,
                        "Message requests sensitive information: $request",
                        Severity.CRITICAL
                    )
                )
            }
        }
        
        // Check for urgency tactics
        URGENCY_KEYWORDS.forEach { keyword ->
            if (messageText.contains(keyword)) {
                riskFactors.add(
                    RiskFactor(
                        RiskType.URGENCY_TACTICS,
                        "Uses urgency tactics: $keyword",
                        Severity.MEDIUM
                    )
                )
            }
        }
        
        // Check for lottery scams
        if (messageText.contains("won") && messageText.contains("lottery") ||
            messageText.contains("congratulations") && messageText.contains("prize")) {
            riskFactors.add(
                RiskFactor(
                    RiskType.LOTTERY_SCAM,
                    "Contains lottery/prize scam indicators",
                    Severity.HIGH
                )
            )
        }
        
        // Check for reversal scams
        if (messageText.contains("reversal") && messageText.contains("confirm")) {
            riskFactors.add(
                RiskFactor(
                    RiskType.REVERSAL_SCAM,
                    "Claims transaction reversal requiring confirmation",
                    Severity.HIGH
                )
            )
        }
        
        // Calculate confidence level
        val highRiskCount = riskFactors.count { it.severity == Severity.HIGH || it.severity == Severity.CRITICAL }
        val mediumRiskCount = riskFactors.count { it.severity == Severity.MEDIUM }
        
        val confidenceLevel = when {
            highRiskCount >= 2 -> 0.9f
            highRiskCount >= 1 -> 0.7f
            mediumRiskCount >= 2 -> 0.6f
            mediumRiskCount >= 1 -> 0.4f
            else -> 0.1f
        }
        
        val isLikelyScam = confidenceLevel >= 0.6f
        
        // Generate recommendations
        generateRecommendations(riskFactors, recommendations)
        
        Log.i(TAG, "Scam analysis completed. Confidence: $confidenceLevel, Likely scam: $isLikelyScam")
        
        return ScamAnalysis(
            isLikelyScam = isLikelyScam,
            confidenceLevel = confidenceLevel,
            detectedPatterns = detectedPatterns,
            riskFactors = riskFactors,
            recommendations = recommendations
        )
    }

    /**
     * Check if phone number is in known scammer database
     */
    fun checkScammerDatabase(phoneNumber: String): Boolean {
        // In production, this would check against a maintained database
        // of known scammer numbers, possibly crowd-sourced
        
        val knownScammerPatterns = listOf(
            "0722.*", // Example pattern - would be actual known numbers
            "0733.*"  // This is just a placeholder
        )
        
        return knownScammerPatterns.any { pattern ->
            Pattern.compile(pattern).matcher(phoneNumber).matches()
        }
    }

    /**
     * Generate scam report for authorities
     */
    fun generateScamReport(
        analysis: ScamAnalysis,
        messageBody: String,
        senderNumber: String?,
        userReport: String?
    ): String {
        val report = StringBuilder()
        
        report.appendLine("M-PESA SCAM REPORT")
        report.appendLine("=" .repeat(40))
        report.appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
        report.appendLine("Confidence Level: ${(analysis.confidenceLevel * 100).toInt()}%")
        report.appendLine("Likely Scam: ${if (analysis.isLikelyScam) "YES" else "NO"}")
        report.appendLine()
        
        report.appendLine("MESSAGE DETAILS:")
        report.appendLine("Sender: ${senderNumber ?: "Unknown"}")
        report.appendLine("Content: $messageBody")
        report.appendLine()
        
        if (analysis.riskFactors.isNotEmpty()) {
            report.appendLine("DETECTED RISK FACTORS:")
            analysis.riskFactors.forEach { factor ->
                report.appendLine("- ${factor.description} (${factor.severity})")
            }
            report.appendLine()
        }
        
        if (userReport != null) {
            report.appendLine("USER REPORT:")
            report.appendLine(userReport)
            report.appendLine()
        }
        
        report.appendLine("RECOMMENDATIONS:")
        analysis.recommendations.forEach { recommendation ->
            report.appendLine("- $recommendation")
        }
        
        return report.toString()
    }

    private fun generateRecommendations(
        riskFactors: List<RiskFactor>,
        recommendations: MutableList<String>
    ) {
        val hasHighRisk = riskFactors.any { it.severity == Severity.HIGH || it.severity == Severity.CRITICAL }
        val hasPinRequest = riskFactors.any { it.type == RiskType.PIN_REQUEST }
        val hasLotteryScam = riskFactors.any { it.type == RiskType.LOTTERY_SCAM }
        
        if (hasHighRisk) {
            recommendations.add("DO NOT respond to this message")
            recommendations.add("Block the sender immediately")
            recommendations.add("Report to Safaricom fraud department: 0722000000")
        }
        
        if (hasPinRequest) {
            recommendations.add("NEVER share your M-Pesa PIN with anyone")
            recommendations.add("Safaricom will never ask for your PIN via SMS")
            recommendations.add("Report PIN request scams to DCI Cybercrime Unit")
        }
        
        if (hasLotteryScam) {
            recommendations.add("Ignore lottery/prize claims you didn't enter")
            recommendations.add("Legitimate promotions don't require upfront payments")
        }
        
        recommendations.add("When in doubt, visit a Safaricom shop for verification")
        recommendations.add("Report this incident through SafeNet Shield")
        recommendations.add("Share this scam pattern with friends and family")
    }

    /**
     * Get statistics about detected scam patterns
     */
    fun getScamStatistics(): Map<String, Int> {
        // In production, this would return real statistics from a database
        return mapOf(
            "total_scams_detected" to 0,
            "pin_request_scams" to 0,
            "lottery_scams" to 0,
            "reversal_scams" to 0,
            "fake_mpesa_messages" to 0
        )
    }
}
