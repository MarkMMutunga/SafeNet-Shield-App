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

package com.safenet.shield.community

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Community Safety Intelligence System
 * Creates a network of users sharing safety information in real-time
 */
class CommunityIntelligenceSystem(private val context: Context) {

    companion object {
        private const val TAG = "CommunityIntelligence"
        private const val COLLECTION_SAFETY_ALERTS = "community_safety_alerts"
        private const val COLLECTION_SCAM_PATTERNS = "community_scam_patterns"
        private const val COLLECTION_SAFE_LOCATIONS = "community_safe_locations"
        private const val PROXIMITY_RADIUS_KM = 10.0 // 10km radius for local alerts
    }

    private val firestore = FirebaseFirestore.getInstance()

    data class SafetyAlert(
        val id: String = UUID.randomUUID().toString(),
        val alertType: AlertType,
        val title: String,
        val description: String,
        val location: GeoPoint?,
        val timestamp: Long = System.currentTimeMillis(),
        val reporterHash: String, // Anonymous reporter ID
        val severity: AlertSeverity,
        val verificationCount: Int = 0,
        val isVerified: Boolean = false,
        val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
        val tags: List<String> = emptyList()
    )

    enum class AlertType {
        SCAM_HOTSPOT,           // Area with high scam activity
        FAKE_WEBSITE_SPOTTED,   // New fake website detected
        PHISHING_CAMPAIGN,      // Active phishing campaign
        MPESA_SCAM_WAVE,        // Multiple M-Pesa scams reported
        SOCIAL_MEDIA_THREAT,    // Platform-specific threats
        ROMANCE_SCAM_PROFILE,   // Dating app scammer identified
        JOB_SCAM_COMPANY,       // Fake company recruiting
        IDENTITY_THEFT_RISK,    // Data breach or ID theft risk
        CYBERBULLYING_TREND,    // Emerging bullying patterns
        TECH_SUPPORT_SCAM       // Fake tech support calls
    }

    enum class AlertSeverity {
        LOW,        // General awareness
        MEDIUM,     // Caution advised
        HIGH,       // Immediate attention
        CRITICAL    // Urgent community action needed
    }

    data class ScamPattern(
        val id: String = UUID.randomUUID().toString(),
        val patternType: String,
        val description: String,
        val commonPhrases: List<String>,
        val reportCount: Int = 1,
        val lastSeen: Long = System.currentTimeMillis(),
        val effectiveness: Float = 0.0f, // How often this pattern succeeds
        val countermeasures: List<String> = emptyList()
    )

    data class SafeLocation(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val location: GeoPoint,
        val type: SafeLocationType,
        val description: String,
        val verificationCount: Int = 0,
        val lastVerified: Long = System.currentTimeMillis()
    )

    enum class SafeLocationType {
        POLICE_STATION,
        HOSPITAL,
        SAFARICOM_SHOP,
        BANK_BRANCH,
        GOVERNMENT_OFFICE,
        CYBERCAFE_TRUSTED,
        COMMUNITY_CENTER,
        UNIVERSITY,
        SHOPPING_MALL
    }

    /**
     * Submit a community safety alert
     */
    suspend fun submitSafetyAlert(alert: SafetyAlert): Result<String> {
        return try {
            // Add location-based relevance scoring
            val enhancedAlert = alert.copy(
                tags = generateSmartTags(alert)
            )

            firestore.collection(COLLECTION_SAFETY_ALERTS)
                .document(alert.id)
                .set(enhancedAlert)
                .await()

            // Notify nearby users
            notifyNearbyUsers(enhancedAlert)

            Log.i(TAG, "Safety alert submitted: ${alert.id}")
            Result.success(alert.id)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit safety alert", e)
            Result.failure(e)
        }
    }

    /**
     * Get nearby safety alerts
     */
    suspend fun getNearbyAlerts(
        userLocation: Location,
        maxDistance: Double = PROXIMITY_RADIUS_KM
    ): Result<List<SafetyAlert>> {
        return try {
            val alerts = firestore.collection(COLLECTION_SAFETY_ALERTS)
                .whereGreaterThan("expiresAt", System.currentTimeMillis())
                .orderBy("expiresAt")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
                .toObjects(SafetyAlert::class.java)

            // Filter by distance
            val nearbyAlerts = alerts.filter { alert ->
                alert.location?.let { alertLocation ->
                    val distance = calculateDistance(
                        userLocation.latitude, userLocation.longitude,
                        alertLocation.latitude, alertLocation.longitude
                    )
                    distance <= maxDistance
                } ?: false
            }

            Result.success(nearbyAlerts)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get nearby alerts", e)
            Result.failure(e)
        }
    }

    /**
     * Report and learn from new scam patterns
     */
    suspend fun submitScamPattern(
        patternType: String,
        description: String,
        phrases: List<String>,
        example: String
    ): Result<String> {
        return try {
            // Check if similar pattern exists
            val existingPatterns = firestore.collection(COLLECTION_SCAM_PATTERNS)
                .whereEqualTo("patternType", patternType)
                .get()
                .await()
                .toObjects(ScamPattern::class.java)

            val pattern = if (existingPatterns.isNotEmpty()) {
                // Update existing pattern
                val existing = existingPatterns.first()
                existing.copy(
                    reportCount = existing.reportCount + 1,
                    lastSeen = System.currentTimeMillis(),
                    commonPhrases = (existing.commonPhrases + phrases).distinct()
                )
            } else {
                // Create new pattern
                ScamPattern(
                    patternType = patternType,
                    description = description,
                    commonPhrases = phrases
                )
            }

            firestore.collection(COLLECTION_SCAM_PATTERNS)
                .document(pattern.id)
                .set(pattern)
                .await()

            // Generate community alert if pattern is becoming common
            if (pattern.reportCount >= 3) {
                generateTrendAlert(pattern)
            }

            Result.success(pattern.id)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit scam pattern", e)
            Result.failure(e)
        }
    }

    /**
     * Get trending scam patterns in your area
     */
    suspend fun getTrendingScamPatterns(limit: Int = 10): Result<List<ScamPattern>> {
        return try {
            val patterns = firestore.collection(COLLECTION_SCAM_PATTERNS)
                .whereGreaterThan("reportCount", 2)
                .orderBy("reportCount", Query.Direction.DESCENDING)
                .orderBy("lastSeen", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .toObjects(ScamPattern::class.java)

            Result.success(patterns)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trending patterns", e)
            Result.failure(e)
        }
    }

    /**
     * Verify a safety alert (community moderation)
     */
    suspend fun verifyAlert(alertId: String, isLegitimate: Boolean): Result<Boolean> {
        return try {
            val alertRef = firestore.collection(COLLECTION_SAFETY_ALERTS).document(alertId)
            val alert = alertRef.get().await().toObject(SafetyAlert::class.java)

            alert?.let {
                val updatedAlert = if (isLegitimate) {
                    it.copy(
                        verificationCount = it.verificationCount + 1,
                        isVerified = it.verificationCount + 1 >= 3 // Require 3 verifications
                    )
                } else {
                    it.copy(verificationCount = it.verificationCount - 1)
                }

                alertRef.set(updatedAlert).await()
                Result.success(true)
            } ?: Result.failure(Exception("Alert not found"))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify alert", e)
            Result.failure(e)
        }
    }

    /**
     * Get community safety score for an area
     */
    suspend fun getAreaSafetyScore(location: GeoPoint, radiusKm: Double = 5.0): Result<SafetyScore> {
        return try {
            val alerts = firestore.collection(COLLECTION_SAFETY_ALERTS)
                .whereGreaterThan("timestamp", System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) // Last 7 days
                .get()
                .await()
                .toObjects(SafetyAlert::class.java)

            val nearbyAlerts = alerts.filter { alert ->
                alert.location?.let { alertLocation ->
                    calculateDistance(
                        location.latitude, location.longitude,
                        alertLocation.latitude, alertLocation.longitude
                    ) <= radiusKm
                } ?: false
            }

            val safetyScore = calculateSafetyScore(nearbyAlerts)
            Result.success(safetyScore)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate safety score", e)
            Result.failure(e)
        }
    }

    data class SafetyScore(
        val score: Float, // 0.0 (very dangerous) to 1.0 (very safe)
        val level: SafetyLevel,
        val recentIncidents: Int,
        val majorConcerns: List<AlertType>,
        val recommendation: String
    )

    enum class SafetyLevel {
        VERY_SAFE, SAFE, MODERATE, RISKY, DANGEROUS
    }

    // Private helper methods
    private fun generateSmartTags(alert: SafetyAlert): List<String> {
        val tags = mutableListOf<String>()
        
        when (alert.alertType) {
            AlertType.MPESA_SCAM_WAVE -> tags.addAll(listOf("mpesa", "mobile-money", "sms"))
            AlertType.ROMANCE_SCAM_PROFILE -> tags.addAll(listOf("dating", "social-media", "relationship"))
            AlertType.JOB_SCAM_COMPANY -> tags.addAll(listOf("employment", "recruitment", "whatsapp"))
            AlertType.PHISHING_CAMPAIGN -> tags.addAll(listOf("email", "banking", "credentials"))
            else -> tags.add(alert.alertType.name.lowercase())
        }

        // Add time-based tags
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> tags.add("morning")
            in 12..17 -> tags.add("afternoon")
            in 18..21 -> tags.add("evening")
            else -> tags.add("night")
        }

        return tags
    }

    private suspend fun notifyNearbyUsers(alert: SafetyAlert) {
        // Implementation would send push notifications to users in the area
        Log.d(TAG, "Notifying nearby users about alert: ${alert.title}")
    }

    private suspend fun generateTrendAlert(pattern: ScamPattern) {
        val alert = SafetyAlert(
            alertType = AlertType.SCAM_HOTSPOT,
            title = "New Scam Pattern Detected",
            description = "Community reports show increasing activity: ${pattern.description}",
            location = null, // Regional alert
            severity = AlertSeverity.MEDIUM,
            reporterHash = "SYSTEM"
        )
        
        submitSafetyAlert(alert)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Haversine formula for calculating distance between two points on Earth
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }

    private fun calculateSafetyScore(alerts: List<SafetyAlert>): SafetyScore {
        if (alerts.isEmpty()) {
            return SafetyScore(
                score = 0.8f,
                level = SafetyLevel.SAFE,
                recentIncidents = 0,
                majorConcerns = emptyList(),
                recommendation = "No recent incidents reported in this area"
            )
        }

        val highSeverityCount = alerts.count { it.severity == AlertSeverity.HIGH || it.severity == AlertSeverity.CRITICAL }
        val totalAlerts = alerts.size
        
        val baseScore = when {
            highSeverityCount == 0 && totalAlerts <= 2 -> 0.9f
            highSeverityCount == 0 && totalAlerts <= 5 -> 0.7f
            highSeverityCount <= 1 -> 0.5f
            highSeverityCount <= 3 -> 0.3f
            else -> 0.1f
        }

        val level = when {
            baseScore >= 0.8f -> SafetyLevel.VERY_SAFE
            baseScore >= 0.6f -> SafetyLevel.SAFE
            baseScore >= 0.4f -> SafetyLevel.MODERATE
            baseScore >= 0.2f -> SafetyLevel.RISKY
            else -> SafetyLevel.DANGEROUS
        }

        val majorConcerns = alerts
            .filter { it.severity == AlertSeverity.HIGH || it.severity == AlertSeverity.CRITICAL }
            .map { it.alertType }
            .distinct()

        val recommendation = when (level) {
            SafetyLevel.VERY_SAFE -> "Area appears safe with minimal recent incidents"
            SafetyLevel.SAFE -> "Generally safe area with some minor incidents"
            SafetyLevel.MODERATE -> "Exercise normal caution, some incidents reported"
            SafetyLevel.RISKY -> "Exercise increased caution, multiple incidents reported"
            SafetyLevel.DANGEROUS -> "High risk area, consider avoiding or taking extra precautions"
        }

        return SafetyScore(
            score = baseScore,
            level = level,
            recentIncidents = totalAlerts,
            majorConcerns = majorConcerns,
            recommendation = recommendation
        )
    }
}
