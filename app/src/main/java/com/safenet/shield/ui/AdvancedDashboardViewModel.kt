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

package com.safenet.shield.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.safenet.shield.ai.DigitalSafetyAssistant
import com.safenet.shield.analytics.SecurityAnalyticsDashboard
import com.safenet.shield.community.CommunityIntelligenceSystem
import com.safenet.shield.ml.MLThreatPredictor

/**
 * ViewModel for Advanced Safety Dashboard
 * Manages data and state for the main dashboard UI
 */
class AdvancedDashboardViewModel : ViewModel() {

    // Security Score
    private val _securityScore = MutableLiveData<Int>()
    val securityScore: LiveData<Int> = _securityScore

    // Threat Level (0.0 to 1.0)
    private val _threatLevel = MutableLiveData<Float>()
    val threatLevel: LiveData<Float> = _threatLevel

    // Safety Recommendations
    private val _safetyRecommendations = MutableLiveData<List<DigitalSafetyAssistant.SafetyRecommendation>>()
    val safetyRecommendations: LiveData<List<DigitalSafetyAssistant.SafetyRecommendation>> = _safetyRecommendations

    // Community Alerts
    private val _communityAlerts = MutableLiveData<List<CommunityIntelligenceSystem.SafetyAlert>>()
    val communityAlerts: LiveData<List<CommunityIntelligenceSystem.SafetyAlert>> = _communityAlerts

    // Threat Predictions
    private val _threatPredictions = MutableLiveData<List<MLThreatPredictor.ThreatPrediction>>()
    val threatPredictions: LiveData<List<MLThreatPredictor.ThreatPrediction>> = _threatPredictions

    // Analytics Dashboard
    private val _analyticsDashboard = MutableLiveData<SecurityAnalyticsDashboard.SecurityDashboard>()
    val analyticsDashboard: LiveData<SecurityAnalyticsDashboard.SecurityDashboard> = _analyticsDashboard

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error State
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Update safety assessment data
     */
    fun updateSafetyAssessment(assessment: DigitalSafetyAssistant.SafetyAssessment) {
        _securityScore.value = calculateSecurityScore(assessment)
        _threatLevel.value = calculateThreatLevel(assessment)
        _safetyRecommendations.value = assessment.personalizedRecommendations
    }

    /**
     * Update analytics dashboard data
     */
    fun updateAnalyticsDashboard(dashboard: SecurityAnalyticsDashboard.SecurityDashboard) {
        _analyticsDashboard.value = dashboard
        
        // Update security score based on analytics
        val analyticsScore = dashboard.overviewMetrics.securityScore
        if (_securityScore.value == null || analyticsScore > 0) {
            _securityScore.value = analyticsScore
        }
    }

    /**
     * Update community alerts
     */
    fun updateCommunityAlerts(alerts: List<CommunityIntelligenceSystem.SafetyAlert>) {
        _communityAlerts.value = alerts.take(10) // Limit to top 10 alerts
        
        // Update threat level based on nearby alerts
        val alertThreatLevel = calculateAlertThreatLevel(alerts)
        val currentThreatLevel = _threatLevel.value ?: 0f
        _threatLevel.value = maxOf(currentThreatLevel, alertThreatLevel)
    }

    /**
     * Update threat predictions
     */
    fun updateThreatPredictions(predictions: List<MLThreatPredictor.ThreatPrediction>) {
        _threatPredictions.value = predictions.sortedByDescending { it.probability }.take(5)
        
        // Update threat level based on predictions
        val predictionThreatLevel = calculatePredictionThreatLevel(predictions)
        val currentThreatLevel = _threatLevel.value ?: 0f
        _threatLevel.value = maxOf(currentThreatLevel, predictionThreatLevel)
    }

    /**
     * Set loading state
     */
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Set error message
     */
    fun setError(message: String) {
        _errorMessage.value = message
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get dashboard summary
     */
    fun getDashboardSummary(): DashboardSummary {
        return DashboardSummary(
            securityScore = _securityScore.value ?: 0,
            threatLevel = _threatLevel.value ?: 0f,
            activeAlerts = _communityAlerts.value?.size ?: 0,
            highPriorityRecommendations = _safetyRecommendations.value?.count { 
                it.priority == DigitalSafetyAssistant.Priority.URGENT || 
                it.priority == DigitalSafetyAssistant.Priority.HIGH 
            } ?: 0,
            criticalPredictions = _threatPredictions.value?.count { 
                it.riskLevel.ordinal >= MLThreatPredictor.RiskLevel.HIGH.ordinal 
            } ?: 0
        )
    }

    // Helper methods
    private fun calculateSecurityScore(assessment: DigitalSafetyAssistant.SafetyAssessment): Int {
        val baseScore = when (assessment.overallRiskLevel) {
            DigitalSafetyAssistant.RiskLevel.VERY_LOW -> 95
            DigitalSafetyAssistant.RiskLevel.LOW -> 80
            DigitalSafetyAssistant.RiskLevel.MODERATE -> 65
            DigitalSafetyAssistant.RiskLevel.HIGH -> 40
            DigitalSafetyAssistant.RiskLevel.CRITICAL -> 20
        }
        
        // Adjust based on confidence
        val confidenceAdjustment = (assessment.confidenceScore * 10).toInt()
        return (baseScore + confidenceAdjustment).coerceIn(0, 100)
    }

    private fun calculateThreatLevel(assessment: DigitalSafetyAssistant.SafetyAssessment): Float {
        return when (assessment.overallRiskLevel) {
            DigitalSafetyAssistant.RiskLevel.VERY_LOW -> 0.1f
            DigitalSafetyAssistant.RiskLevel.LOW -> 0.3f
            DigitalSafetyAssistant.RiskLevel.MODERATE -> 0.5f
            DigitalSafetyAssistant.RiskLevel.HIGH -> 0.8f
            DigitalSafetyAssistant.RiskLevel.CRITICAL -> 1.0f
        }
    }

    private fun calculateAlertThreatLevel(alerts: List<CommunityIntelligenceSystem.SafetyAlert>): Float {
        if (alerts.isEmpty()) return 0f
        
        val highSeverityAlerts = alerts.count { 
            it.severity == CommunityIntelligenceSystem.AlertSeverity.HIGH || 
            it.severity == CommunityIntelligenceSystem.AlertSeverity.CRITICAL 
        }
        
        return when {
            highSeverityAlerts >= 3 -> 0.9f
            highSeverityAlerts >= 2 -> 0.7f
            highSeverityAlerts >= 1 -> 0.5f
            alerts.size >= 5 -> 0.4f
            else -> 0.2f
        }
    }

    private fun calculatePredictionThreatLevel(predictions: List<MLThreatPredictor.ThreatPrediction>): Float {
        if (predictions.isEmpty()) return 0f
        
        val maxProbability = predictions.maxOfOrNull { it.probability } ?: 0f
        val criticalPredictions = predictions.count { 
            it.riskLevel.ordinal >= MLThreatPredictor.RiskLevel.HIGH.ordinal 
        }
        
        return when {
            maxProbability >= 0.8f && criticalPredictions >= 2 -> 0.9f
            maxProbability >= 0.7f || criticalPredictions >= 2 -> 0.7f
            maxProbability >= 0.5f || criticalPredictions >= 1 -> 0.5f
            maxProbability >= 0.3f -> 0.3f
            else -> 0.1f
        }
    }

    /**
     * Data class for dashboard summary
     */
    data class DashboardSummary(
        val securityScore: Int,
        val threatLevel: Float,
        val activeAlerts: Int,
        val highPriorityRecommendations: Int,
        val criticalPredictions: Int
    ) {
        val overallStatus: String
            get() = when {
                securityScore >= 80 && threatLevel <= 0.3f -> "Secure"
                securityScore >= 60 && threatLevel <= 0.5f -> "Moderate"
                securityScore >= 40 && threatLevel <= 0.7f -> "At Risk"
                else -> "High Risk"
            }
    }
}
