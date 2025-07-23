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

package com.safenet.shield.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Enhanced Security Analytics Dashboard
 * Provides comprehensive security insights, trends, and predictive analytics
 */
class SecurityAnalyticsDashboard(private val context: Context) {

    companion object {
        private const val TAG = "SecurityAnalytics"
        private const val ANALYTICS_COLLECTION = "security_analytics"
        private const val TRENDS_COLLECTION = "security_trends"
        private const val INSIGHTS_COLLECTION = "security_insights"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class SecurityDashboard(
        val overviewMetrics: SecurityOverview,
        val threatTrends: List<ThreatTrend>,
        val performanceMetrics: PerformanceMetrics,
        val regionalInsights: List<RegionalInsight>,
        val predictiveAnalytics: PredictiveInsights,
        val recommendations: List<SecurityRecommendation>,
        val lastUpdated: Long = System.currentTimeMillis()
    )

    data class SecurityOverview(
        val totalIncidents: Int,
        val resolvedIncidents: Int,
        val activeThreats: Int,
        val preventedAttacks: Int,
        val riskReduction: Float, // Percentage
        val securityScore: Int, // 0-100
        val trendDirection: TrendDirection
    )

    data class ThreatTrend(
        val threatType: String,
        val currentCount: Int,
        val previousPeriodCount: Int,
        val percentageChange: Float,
        val severity: ThreatSeverity,
        val timeframe: String,
        val affectedRegions: List<String>
    )

    data class PerformanceMetrics(
        val responseTime: ResponseTimeMetrics,
        val detectionAccuracy: Float,
        val falsePositiveRate: Float,
        val systemUptime: Float,
        val userEngagement: UserEngagementMetrics
    )

    data class ResponseTimeMetrics(
        val averageResponseTime: Long, // milliseconds
        val emergencyResponseTime: Long,
        val reportProcessingTime: Long,
        val alertDeliveryTime: Long
    )

    data class UserEngagementMetrics(
        val activeUsers: Int,
        val reportSubmissions: Int,
        val alertInteractions: Int,
        val trainingModulesCompleted: Int,
        val communityParticipation: Float
    )

    data class RegionalInsight(
        val region: String,
        val coordinates: Pair<Double, Double>,
        val riskLevel: RiskLevel,
        val primaryThreat: String,
        val incidentCount: Int,
        val populationAtRisk: Int,
        val safetyScore: Float,
        val trendAnalysis: RegionalTrend
    )

    data class RegionalTrend(
        val direction: TrendDirection,
        val changePercent: Float,
        val timeframe: String,
        val contributingFactors: List<String>
    )

    data class PredictiveInsights(
        val threatForecasts: List<ThreatForecast>,
        val riskPredictions: List<RiskPrediction>,
        val seasonalPatterns: List<SeasonalPattern>,
        val emergingThreats: List<EmergingThreat>,
        val confidenceLevel: Float
    )

    data class ThreatForecast(
        val threatType: String,
        val predictedIncrease: Float,
        val timeframe: String,
        val probability: Float,
        val affectedDemographics: List<String>
    )

    data class RiskPrediction(
        val riskCategory: String,
        val currentRisk: Float,
        val predictedRisk: Float,
        val timeline: String,
        val mitigationStrategies: List<String>
    )

    data class SeasonalPattern(
        val pattern: String,
        val peakMonths: List<String>,
        val intensityIncrease: Float,
        val historicalData: List<DataPoint>
    )

    data class DataPoint(
        val timestamp: Long,
        val value: Float,
        val category: String
    )

    data class EmergingThreat(
        val threatName: String,
        val description: String,
        val firstDetected: Long,
        val growthRate: Float,
        val potentialImpact: ThreatSeverity,
        val countermeasures: List<String>
    )

    data class SecurityRecommendation(
        val title: String,
        val description: String,
        val priority: RecommendationPriority,
        val category: RecommendationCategory,
        val implementationTime: String,
        val expectedImpact: String,
        val resources: List<String>
    )

    enum class TrendDirection {
        INCREASING, DECREASING, STABLE, VOLATILE
    }

    enum class ThreatSeverity {
        LOW, MODERATE, HIGH, CRITICAL, EXTREME
    }

    enum class RiskLevel {
        MINIMAL, LOW, MODERATE, HIGH, SEVERE
    }

    enum class RecommendationPriority {
        LOW, MEDIUM, HIGH, URGENT, CRITICAL
    }

    enum class RecommendationCategory {
        IMMEDIATE_ACTION,
        SYSTEM_IMPROVEMENT,
        USER_EDUCATION,
        TECHNOLOGY_UPGRADE,
        POLICY_CHANGE
    }

    /**
     * Generate comprehensive security dashboard
     */
    suspend fun generateSecurityDashboard(timeframe: TimeFrame = TimeFrame.LAST_30_DAYS): Result<SecurityDashboard> {
        return try {
            val overview = generateSecurityOverview(timeframe)
            val threats = analyzeThreatTrends(timeframe)
            val performance = calculatePerformanceMetrics(timeframe)
            val regional = generateRegionalInsights(timeframe)
            val predictive = generatePredictiveInsights()
            val recommendations = generateSecurityRecommendations(overview, threats, performance)

            val dashboard = SecurityDashboard(
                overviewMetrics = overview,
                threatTrends = threats,
                performanceMetrics = performance,
                regionalInsights = regional,
                predictiveAnalytics = predictive,
                recommendations = recommendations
            )

            // Cache dashboard for performance
            cacheDashboard(dashboard)

            Result.success(dashboard)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate security dashboard", e)
            Result.failure(e)
        }
    }

    enum class TimeFrame {
        LAST_24_HOURS,
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_90_DAYS,
        LAST_YEAR,
        ALL_TIME
    }

    /**
     * Generate security overview metrics
     */
    private suspend fun generateSecurityOverview(timeframe: TimeFrame): SecurityOverview {
        val timeRange = getTimeRange(timeframe)
        
        val incidentsQuery = firestore.collection("incidents")
            .whereGreaterThan("timestamp", timeRange.start)
            .whereLessThan("timestamp", timeRange.end)

        val incidents = incidentsQuery.get().await()
        val totalIncidents = incidents.size()
        val resolvedIncidents = incidents.documents.count { 
            it.getString("status") == "resolved" 
        }

        val threatsQuery = firestore.collection("active_threats")
            .whereGreaterThan("detected_at", timeRange.start)

        val activeThreats = threatsQuery.get().await().size()

        val preventedAttacks = calculatePreventedAttacks(timeRange)
        val riskReduction = calculateRiskReduction(timeRange)
        val securityScore = calculateSecurityScore(totalIncidents, resolvedIncidents, activeThreats)
        val trendDirection = calculateTrendDirection(timeRange)

        return SecurityOverview(
            totalIncidents = totalIncidents,
            resolvedIncidents = resolvedIncidents,
            activeThreats = activeThreats,
            preventedAttacks = preventedAttacks,
            riskReduction = riskReduction,
            securityScore = securityScore,
            trendDirection = trendDirection
        )
    }

    /**
     * Analyze threat trends over time
     */
    private suspend fun analyzeThreatTrends(timeframe: TimeFrame): List<ThreatTrend> {
        val timeRange = getTimeRange(timeframe)
        val previousTimeRange = getPreviousTimeRange(timeframe)

        val currentThreats = firestore.collection("threats")
            .whereGreaterThan("timestamp", timeRange.start)
            .whereLessThan("timestamp", timeRange.end)
            .get().await()

        val previousThreats = firestore.collection("threats")
            .whereGreaterThan("timestamp", previousTimeRange.start)
            .whereLessThan("timestamp", previousTimeRange.end)
            .get().await()

        val threatTypes = mutableMapOf<String, ThreatData>()

        // Process current period
        currentThreats.documents.forEach { doc ->
            val type = doc.getString("type") ?: "unknown"
            val severity = ThreatSeverity.valueOf(doc.getString("severity") ?: "LOW")
            val region = doc.getString("region") ?: "unknown"

            threatTypes.getOrPut(type) { ThreatData() }.apply {
                currentCount++
                if (severity.ordinal > maxSeverity.ordinal) maxSeverity = severity
                affectedRegions.add(region)
            }
        }

        // Process previous period
        previousThreats.documents.forEach { doc ->
            val type = doc.getString("type") ?: "unknown"
            threatTypes.getOrPut(type) { ThreatData() }.previousCount++
        }

        return threatTypes.map { (type, data) ->
            val percentageChange = if (data.previousCount > 0) {
                ((data.currentCount - data.previousCount).toFloat() / data.previousCount) * 100
            } else if (data.currentCount > 0) {
                100f
            } else {
                0f
            }

            ThreatTrend(
                threatType = type,
                currentCount = data.currentCount,
                previousPeriodCount = data.previousCount,
                percentageChange = percentageChange,
                severity = data.maxSeverity,
                timeframe = timeframe.name,
                affectedRegions = data.affectedRegions.toList()
            )
        }.sortedByDescending { it.currentCount }
    }

    private data class ThreatData(
        var currentCount: Int = 0,
        var previousCount: Int = 0,
        var maxSeverity: ThreatSeverity = ThreatSeverity.LOW,
        val affectedRegions: MutableSet<String> = mutableSetOf()
    )

    /**
     * Calculate performance metrics
     */
    private suspend fun calculatePerformanceMetrics(timeframe: TimeFrame): PerformanceMetrics {
        val timeRange = getTimeRange(timeframe)

        val responseTime = calculateResponseTimeMetrics(timeRange)
        val detectionAccuracy = calculateDetectionAccuracy(timeRange)
        val falsePositiveRate = calculateFalsePositiveRate(timeRange)
        val systemUptime = calculateSystemUptime(timeRange)
        val userEngagement = calculateUserEngagement(timeRange)

        return PerformanceMetrics(
            responseTime = responseTime,
            detectionAccuracy = detectionAccuracy,
            falsePositiveRate = falsePositiveRate,
            systemUptime = systemUptime,
            userEngagement = userEngagement
        )
    }

    /**
     * Generate regional security insights
     */
    private suspend fun generateRegionalInsights(timeframe: TimeFrame): List<RegionalInsight> {
        val timeRange = getTimeRange(timeframe)

        val regionsQuery = firestore.collection("regional_data")
            .whereGreaterThan("last_updated", timeRange.start)

        val regionDocs = regionsQuery.get().await()

        return regionDocs.documents.mapNotNull { doc ->
            val region = doc.getString("name") ?: return@mapNotNull null
            val lat = doc.getDouble("latitude") ?: 0.0
            val lng = doc.getDouble("longitude") ?: 0.0
            val riskLevel = RiskLevel.valueOf(doc.getString("risk_level") ?: "LOW")
            val primaryThreat = doc.getString("primary_threat") ?: "unknown"
            val incidentCount = doc.getLong("incident_count")?.toInt() ?: 0
            val population = doc.getLong("population_at_risk")?.toInt() ?: 0
            val safetyScore = doc.getDouble("safety_score")?.toFloat() ?: 0f

            val trend = calculateRegionalTrend(region, timeRange)

            RegionalInsight(
                region = region,
                coordinates = Pair(lat, lng),
                riskLevel = riskLevel,
                primaryThreat = primaryThreat,
                incidentCount = incidentCount,
                populationAtRisk = population,
                safetyScore = safetyScore,
                trendAnalysis = trend
            )
        }
    }

    /**
     * Generate predictive insights using historical data
     */
    private suspend fun generatePredictiveInsights(): PredictiveInsights {
        val threatForecasts = generateThreatForecasts()
        val riskPredictions = generateRiskPredictions()
        val seasonalPatterns = identifySeasonalPatterns()
        val emergingThreats = detectEmergingThreats()

        return PredictiveInsights(
            threatForecasts = threatForecasts,
            riskPredictions = riskPredictions,
            seasonalPatterns = seasonalPatterns,
            emergingThreats = emergingThreats,
            confidenceLevel = 0.75f
        )
    }

    // Helper methods for calculations
    private fun getTimeRange(timeframe: TimeFrame): TimeRange {
        val now = System.currentTimeMillis()
        val start = when (timeframe) {
            TimeFrame.LAST_24_HOURS -> now - (24 * 60 * 60 * 1000)
            TimeFrame.LAST_7_DAYS -> now - (7 * 24 * 60 * 60 * 1000)
            TimeFrame.LAST_30_DAYS -> now - (30L * 24 * 60 * 60 * 1000)
            TimeFrame.LAST_90_DAYS -> now - (90L * 24 * 60 * 60 * 1000)
            TimeFrame.LAST_YEAR -> now - (365L * 24 * 60 * 60 * 1000)
            TimeFrame.ALL_TIME -> 0L
        }
        return TimeRange(start, now)
    }

    private fun getPreviousTimeRange(timeframe: TimeFrame): TimeRange {
        val current = getTimeRange(timeframe)
        val duration = current.end - current.start
        return TimeRange(current.start - duration, current.start)
    }

    private data class TimeRange(val start: Long, val end: Long)

    private suspend fun calculatePreventedAttacks(timeRange: TimeRange): Int {
        val preventedQuery = firestore.collection("prevented_attacks")
            .whereGreaterThan("timestamp", timeRange.start)
            .whereLessThan("timestamp", timeRange.end)

        return preventedQuery.get().await().size()
    }

    private suspend fun calculateRiskReduction(timeRange: TimeRange): Float {
        // Calculate risk reduction percentage based on prevented vs potential attacks
        val prevented = calculatePreventedAttacks(timeRange)
        val potential = prevented + 10 // Estimated potential attacks
        return if (potential > 0) (prevented.toFloat() / potential) * 100 else 0f
    }

    private fun calculateSecurityScore(total: Int, resolved: Int, active: Int): Int {
        val resolutionRate = if (total > 0) resolved.toFloat() / total else 1f
        val threatPenalty = active * 2
        val baseScore = (resolutionRate * 100).roundToInt()
        return (baseScore - threatPenalty).coerceIn(0, 100)
    }

    private suspend fun calculateTrendDirection(timeRange: TimeRange): TrendDirection {
        // Simple trend calculation based on incident count changes
        val previousRange = TimeRange(
            timeRange.start - (timeRange.end - timeRange.start),
            timeRange.start
        )

        val currentIncidents = firestore.collection("incidents")
            .whereGreaterThan("timestamp", timeRange.start)
            .whereLessThan("timestamp", timeRange.end)
            .get().await().size()

        val previousIncidents = firestore.collection("incidents")
            .whereGreaterThan("timestamp", previousRange.start)
            .whereLessThan("timestamp", previousRange.end)
            .get().await().size()

        return when {
            currentIncidents > previousIncidents * 1.1 -> TrendDirection.INCREASING
            currentIncidents < previousIncidents * 0.9 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
    }

    // Placeholder implementations for complex calculations
    private suspend fun calculateResponseTimeMetrics(timeRange: TimeRange): ResponseTimeMetrics {
        return ResponseTimeMetrics(
            averageResponseTime = 300000, // 5 minutes
            emergencyResponseTime = 60000, // 1 minute
            reportProcessingTime = 120000, // 2 minutes
            alertDeliveryTime = 5000 // 5 seconds
        )
    }

    private suspend fun calculateDetectionAccuracy(timeRange: TimeRange): Float = 0.92f
    private suspend fun calculateFalsePositiveRate(timeRange: TimeRange): Float = 0.05f
    private suspend fun calculateSystemUptime(timeRange: TimeRange): Float = 0.998f

    private suspend fun calculateUserEngagement(timeRange: TimeRange): UserEngagementMetrics {
        return UserEngagementMetrics(
            activeUsers = 1250,
            reportSubmissions = 89,
            alertInteractions = 445,
            trainingModulesCompleted = 156,
            communityParticipation = 0.68f
        )
    }

    private suspend fun calculateRegionalTrend(region: String, timeRange: TimeRange): RegionalTrend {
        return RegionalTrend(
            direction = TrendDirection.STABLE,
            changePercent = 2.5f,
            timeframe = "30 days",
            contributingFactors = listOf("Increased awareness", "Better reporting")
        )
    }

    private suspend fun generateThreatForecasts(): List<ThreatForecast> = emptyList()
    private suspend fun generateRiskPredictions(): List<RiskPrediction> = emptyList()
    private suspend fun identifySeasonalPatterns(): List<SeasonalPattern> = emptyList()
    private suspend fun detectEmergingThreats(): List<EmergingThreat> = emptyList()

    private fun generateSecurityRecommendations(
        overview: SecurityOverview,
        threats: List<ThreatTrend>,
        performance: PerformanceMetrics
    ): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()

        if (overview.securityScore < 70) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Improve Incident Resolution Rate",
                    description = "Current resolution rate is below optimal. Implement faster response procedures.",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.IMMEDIATE_ACTION,
                    implementationTime = "1-2 weeks",
                    expectedImpact = "15-20% improvement in security score",
                    resources = listOf("Additional response team", "Automated tools")
                )
            )
        }

        if (performance.falsePositiveRate > 0.1f) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Reduce False Positive Rate",
                    description = "High false positive rate may be causing alert fatigue.",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.SYSTEM_IMPROVEMENT,
                    implementationTime = "2-4 weeks",
                    expectedImpact = "Better user trust and engagement",
                    resources = listOf("ML model tuning", "Expert review")
                )
            )
        }

        return recommendations
    }

    private suspend fun cacheDashboard(dashboard: SecurityDashboard) {
        try {
            firestore.collection("dashboard_cache")
                .document("latest")
                .set(dashboard)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache dashboard", e)
        }
    }
}
