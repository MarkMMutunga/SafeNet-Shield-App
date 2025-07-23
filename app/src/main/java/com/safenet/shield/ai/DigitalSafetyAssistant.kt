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

package com.safenet.shield.ai

import android.content.Context
import android.util.Log
import com.safenet.shield.cybercrime.MpesaScamDetector
import com.safenet.shield.community.CommunityIntelligenceSystem
import java.util.*

/**
 * AI-Powered Digital Safety Assistant
 * Provides personalized safety guidance and real-time threat analysis
 */
class DigitalSafetyAssistant(private val context: Context) {

    companion object {
        private const val TAG = "SafetyAssistant"
    }

    private val mpesaDetector = MpesaScamDetector(context)
    private val communityIntelligence = CommunityIntelligenceSystem(context)

    data class SafetyAssessment(
        val overallRiskLevel: RiskLevel,
        val personalizedRecommendations: List<SafetyRecommendation>,
        val threatSummary: ThreatAnalysis,
        val actionItems: List<ActionItem>,
        val confidenceScore: Float
    )

    data class SafetyRecommendation(
        val category: RecommendationCategory,
        val title: String,
        val description: String,
        val priority: Priority,
        val actionRequired: Boolean,
        val estimatedTime: String,
        val benefits: List<String>
    )

    data class ThreatAnalysis(
        val activeThreatCount: Int,
        val primaryThreats: List<ThreatType>,
        val trendingScams: List<String>,
        val personalRiskFactors: List<String>,
        val environmentalRisks: List<String>
    )

    data class ActionItem(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val description: String,
        val urgency: Urgency,
        val category: ActionCategory,
        val estimatedDuration: String,
        val dueDate: Long? = null,
        val isCompleted: Boolean = false
    )

    enum class RiskLevel {
        VERY_LOW, LOW, MODERATE, HIGH, CRITICAL
    }

    enum class RecommendationCategory {
        DEVICE_SECURITY,
        ACCOUNT_PROTECTION,
        COMMUNICATION_SAFETY,
        FINANCIAL_SECURITY,
        SOCIAL_MEDIA_PRIVACY,
        SCAM_AWARENESS,
        EMERGENCY_PREPAREDNESS
    }

    enum class Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    enum class ThreatType {
        MPESA_SCAM,
        PHISHING_EMAIL,
        SOCIAL_ENGINEERING,
        IDENTITY_THEFT,
        ACCOUNT_TAKEOVER,
        CYBERBULLYING,
        ROMANCE_SCAM,
        INVESTMENT_SCAM
    }

    enum class Urgency {
        IMMEDIATE,    // Do within hours
        TODAY,        // Do today
        THIS_WEEK,    // Do within a week
        THIS_MONTH,   // Do within a month
        WHEN_CONVENIENT // No specific timeline
    }

    enum class ActionCategory {
        SECURITY_UPDATE,
        PRIVACY_SETTING,
        ACCOUNT_REVIEW,
        BACKUP_DATA,
        LEARN_SKILL,
        REPORT_INCIDENT
    }

    /**
     * Generate comprehensive safety assessment for user
     */
    suspend fun generateSafetyAssessment(userProfile: UserSafetyProfile): SafetyAssessment {
        return try {
            val threatAnalysis = analyzeThreatLandscape(userProfile)
            val recommendations = generatePersonalizedRecommendations(userProfile, threatAnalysis)
            val actionItems = createActionItems(recommendations)
            val riskLevel = calculateOverallRisk(threatAnalysis, userProfile)

            SafetyAssessment(
                overallRiskLevel = riskLevel,
                personalizedRecommendations = recommendations,
                threatSummary = threatAnalysis,
                actionItems = actionItems,
                confidenceScore = calculateConfidence(userProfile)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate safety assessment", e)
            getDefaultSafetyAssessment()
        }
    }

    data class UserSafetyProfile(
        val ageGroup: AgeGroup,
        val techSavviness: TechLevel,
        val primaryPlatforms: List<String>,
        val financialActivity: FinancialActivity,
        val locationRisk: LocationRisk,
        val previousIncidents: List<String>,
        val securityMeasuresEnabled: List<String>,
        val lastSecurityUpdate: Long
    )

    enum class AgeGroup {
        TEEN_13_17, YOUNG_ADULT_18_25, ADULT_26_40, MIDDLE_AGED_41_60, SENIOR_60_PLUS
    }

    enum class TechLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    enum class FinancialActivity {
        BASIC_BANKING, MOBILE_MONEY_USER, ONLINE_SHOPPER, INVESTOR, BUSINESS_OWNER
    }

    enum class LocationRisk {
        RURAL_LOW_RISK, URBAN_MODERATE, CITY_HIGH_TRAFFIC, INTERNATIONAL_TRAVEL
    }

    /**
     * Analyze current threat landscape specific to user
     */
    private suspend fun analyzeThreatLandscape(profile: UserSafetyProfile): ThreatAnalysis {
        val activeThreatCount = getThreatCount(profile)
        val primaryThreats = identifyPrimaryThreats(profile)
        val trendingScams = getTrendingScams()
        val personalRiskFactors = assessPersonalRiskFactors(profile)
        val environmentalRisks = assessEnvironmentalRisks(profile)

        return ThreatAnalysis(
            activeThreatCount = activeThreatCount,
            primaryThreats = primaryThreats,
            trendingScams = trendingScams,
            personalRiskFactors = personalRiskFactors,
            environmentalRisks = environmentalRisks
        )
    }

    /**
     * Generate personalized safety recommendations
     */
    private fun generatePersonalizedRecommendations(
        profile: UserSafetyProfile,
        threats: ThreatAnalysis
    ): List<SafetyRecommendation> {
        val recommendations = mutableListOf<SafetyRecommendation>()

        // Age-specific recommendations
        when (profile.ageGroup) {
            AgeGroup.TEEN_13_17, AgeGroup.YOUNG_ADULT_18_25 -> {
                recommendations.addAll(getYouthRecommendations())
            }
            AgeGroup.SENIOR_60_PLUS -> {
                recommendations.addAll(getSeniorRecommendations())
            }
            else -> {
                recommendations.addAll(getGeneralAdultRecommendations())
            }
        }

        // Tech level adjustments
        when (profile.techSavviness) {
            TechLevel.BEGINNER -> {
                recommendations.addAll(getBeginnerRecommendations())
            }
            TechLevel.EXPERT -> {
                recommendations.addAll(getAdvancedRecommendations())
            }
            else -> {
                recommendations.addAll(getIntermediateRecommendations())
            }
        }

        // Financial activity specific
        if (profile.financialActivity == FinancialActivity.MOBILE_MONEY_USER) {
            recommendations.addAll(getMpesaSecurityRecommendations())
        }

        // Threat-specific recommendations
        threats.primaryThreats.forEach { threat ->
            recommendations.addAll(getThreatSpecificRecommendations(threat))
        }

        return recommendations.distinctBy { it.title }.sortedByDescending { it.priority.ordinal }
    }

    /**
     * Create actionable items from recommendations
     */
    private fun createActionItems(recommendations: List<SafetyRecommendation>): List<ActionItem> {
        return recommendations.filter { it.actionRequired }.map { recommendation ->
            val urgency = when (recommendation.priority) {
                Priority.URGENT -> Urgency.IMMEDIATE
                Priority.HIGH -> Urgency.TODAY
                Priority.MEDIUM -> Urgency.THIS_WEEK
                Priority.LOW -> Urgency.THIS_MONTH
            }

            val category = when (recommendation.category) {
                RecommendationCategory.DEVICE_SECURITY -> ActionCategory.SECURITY_UPDATE
                RecommendationCategory.ACCOUNT_PROTECTION -> ActionCategory.ACCOUNT_REVIEW
                RecommendationCategory.SOCIAL_MEDIA_PRIVACY -> ActionCategory.PRIVACY_SETTING
                else -> ActionCategory.LEARN_SKILL
            }

            ActionItem(
                title = recommendation.title,
                description = recommendation.description,
                urgency = urgency,
                category = category,
                estimatedDuration = recommendation.estimatedTime,
                dueDate = calculateDueDate(urgency)
            )
        }
    }

    // Recommendation generators for different user types
    private fun getYouthRecommendations(): List<SafetyRecommendation> {
        return listOf(
            SafetyRecommendation(
                category = RecommendationCategory.SOCIAL_MEDIA_PRIVACY,
                title = "Review Social Media Privacy Settings",
                description = "Check privacy settings on all social platforms to limit who can see your posts and personal information",
                priority = Priority.HIGH,
                actionRequired = true,
                estimatedTime = "15 minutes",
                benefits = listOf("Prevent strangers from accessing personal info", "Reduce cyberbullying risk", "Protect family information")
            ),
            SafetyRecommendation(
                category = RecommendationCategory.SCAM_AWARENESS,
                title = "Learn About Romance Scams",
                description = "Understand how online predators target young people through dating apps and social media",
                priority = Priority.MEDIUM,
                actionRequired = false,
                estimatedTime = "10 minutes",
                benefits = listOf("Avoid emotional manipulation", "Protect personal information", "Recognize warning signs early")
            )
        )
    }

    private fun getSeniorRecommendations(): List<SafetyRecommendation> {
        return listOf(
            SafetyRecommendation(
                category = RecommendationCategory.SCAM_AWARENESS,
                title = "Recognize Tech Support Scams",
                description = "Learn to identify fake calls claiming your computer is infected or needs immediate attention",
                priority = Priority.URGENT,
                actionRequired = true,
                estimatedTime = "20 minutes",
                benefits = listOf("Avoid financial loss", "Protect personal data", "Maintain computer security")
            ),
            SafetyRecommendation(
                category = RecommendationCategory.FINANCIAL_SECURITY,
                title = "Set Up Bank Account Alerts",
                description = "Enable SMS or email notifications for all banking transactions to detect unauthorized activity",
                priority = Priority.HIGH,
                actionRequired = true,
                estimatedTime = "10 minutes",
                benefits = listOf("Early fraud detection", "Peace of mind", "Quick response to unauthorized transactions")
            )
        )
    }

    private fun getMpesaSecurityRecommendations(): List<SafetyRecommendation> {
        return listOf(
            SafetyRecommendation(
                category = RecommendationCategory.FINANCIAL_SECURITY,
                title = "Strengthen M-Pesa PIN Security",
                description = "Change your M-Pesa PIN regularly and never share it with anyone, including people claiming to be from Safaricom",
                priority = Priority.URGENT,
                actionRequired = true,
                estimatedTime = "5 minutes",
                benefits = listOf("Prevent unauthorized transactions", "Protect savings", "Maintain account security")
            ),
            SafetyRecommendation(
                category = RecommendationCategory.SCAM_AWARENESS,
                title = "Recognize M-Pesa Scam Messages",
                description = "Learn to identify fake transaction confirmations and reversal requests",
                priority = Priority.HIGH,
                actionRequired = false,
                estimatedTime = "10 minutes",
                benefits = listOf("Avoid financial loss", "Protect PIN security", "Help family members stay safe")
            )
        )
    }

    private fun getThreatSpecificRecommendations(threat: ThreatType): List<SafetyRecommendation> {
        return when (threat) {
            ThreatType.PHISHING_EMAIL -> listOf(
                SafetyRecommendation(
                    category = RecommendationCategory.COMMUNICATION_SAFETY,
                    title = "Enable Email Security Features",
                    description = "Turn on spam filtering and phishing protection in your email client",
                    priority = Priority.HIGH,
                    actionRequired = true,
                    estimatedTime = "5 minutes",
                    benefits = listOf("Block malicious emails", "Protect credentials", "Reduce exposure to scams")
                )
            )
            ThreatType.IDENTITY_THEFT -> listOf(
                SafetyRecommendation(
                    category = RecommendationCategory.ACCOUNT_PROTECTION,
                    title = "Monitor Your Digital Identity",
                    description = "Regularly check your online accounts and profiles for unauthorized changes",
                    priority = Priority.MEDIUM,
                    actionRequired = false,
                    estimatedTime = "15 minutes weekly",
                    benefits = listOf("Early identity theft detection", "Protect personal reputation", "Maintain account control")
                )
            )
            else -> emptyList()
        }
    }

    // Helper methods
    private fun getThreatCount(profile: UserSafetyProfile): Int {
        // Calculate based on profile risk factors
        var count = 0
        if (profile.financialActivity == FinancialActivity.MOBILE_MONEY_USER) count++
        if (profile.techSavviness == TechLevel.BEGINNER) count++
        if (profile.previousIncidents.isNotEmpty()) count += profile.previousIncidents.size
        return count
    }

    private fun identifyPrimaryThreats(profile: UserSafetyProfile): List<ThreatType> {
        val threats = mutableListOf<ThreatType>()
        
        if (profile.financialActivity == FinancialActivity.MOBILE_MONEY_USER) {
            threats.add(ThreatType.MPESA_SCAM)
        }
        
        if (profile.ageGroup in listOf(AgeGroup.TEEN_13_17, AgeGroup.YOUNG_ADULT_18_25)) {
            threats.add(ThreatType.CYBERBULLYING)
            threats.add(ThreatType.ROMANCE_SCAM)
        }
        
        if (profile.ageGroup == AgeGroup.SENIOR_60_PLUS) {
            threats.add(ThreatType.SOCIAL_ENGINEERING)
            threats.add(ThreatType.INVESTMENT_SCAM)
        }
        
        return threats
    }

    private suspend fun getTrendingScams(): List<String> {
        return try {
            val patterns = communityIntelligence.getTrendingScamPatterns(5)
            patterns.getOrNull()?.map { it.description } ?: emptyList()
        } catch (e: Exception) {
            listOf("M-Pesa reversal scams", "Fake job opportunities", "Romance scams on dating apps")
        }
    }

    private fun assessPersonalRiskFactors(profile: UserSafetyProfile): List<String> {
        val factors = mutableListOf<String>()
        
        if (profile.techSavviness == TechLevel.BEGINNER) {
            factors.add("Limited technical knowledge increases vulnerability")
        }
        
        if (profile.securityMeasuresEnabled.isEmpty()) {
            factors.add("No security measures currently enabled")
        }
        
        if (System.currentTimeMillis() - profile.lastSecurityUpdate > (90 * 24 * 60 * 60 * 1000L)) {
            factors.add("Security settings not reviewed in 90+ days")
        }
        
        return factors
    }

    private fun assessEnvironmentalRisks(profile: UserSafetyProfile): List<String> {
        val risks = mutableListOf<String>()
        
        when (profile.locationRisk) {
            LocationRisk.CITY_HIGH_TRAFFIC -> risks.add("Urban environment with higher cybercrime rates")
            LocationRisk.INTERNATIONAL_TRAVEL -> risks.add("Travel increases exposure to location-based scams")
            LocationRisk.RURAL_LOW_RISK -> risks.add("Limited local cybersecurity resources")
            else -> {}
        }
        
        return risks
    }

    private fun calculateOverallRisk(threats: ThreatAnalysis, profile: UserSafetyProfile): RiskLevel {
        var riskScore = 0
        
        riskScore += threats.activeThreatCount * 2
        riskScore += threats.personalRiskFactors.size
        riskScore += when (profile.techSavviness) {
            TechLevel.BEGINNER -> 3
            TechLevel.INTERMEDIATE -> 1
            else -> 0
        }
        
        return when {
            riskScore <= 2 -> RiskLevel.VERY_LOW
            riskScore <= 4 -> RiskLevel.LOW
            riskScore <= 7 -> RiskLevel.MODERATE
            riskScore <= 10 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }

    private fun calculateConfidence(profile: UserSafetyProfile): Float {
        // Higher confidence for profiles with more complete information
        var confidence = 0.5f
        
        if (profile.previousIncidents.isNotEmpty()) confidence += 0.2f
        if (profile.securityMeasuresEnabled.isNotEmpty()) confidence += 0.2f
        if (profile.lastSecurityUpdate > 0) confidence += 0.1f
        
        return confidence.coerceAtMost(1.0f)
    }

    private fun calculateDueDate(urgency: Urgency): Long {
        val currentTime = System.currentTimeMillis()
        return when (urgency) {
            Urgency.IMMEDIATE -> currentTime + (4 * 60 * 60 * 1000) // 4 hours
            Urgency.TODAY -> currentTime + (24 * 60 * 60 * 1000) // 24 hours
            Urgency.THIS_WEEK -> currentTime + (7 * 24 * 60 * 60 * 1000) // 7 days
            Urgency.THIS_MONTH -> currentTime + (30L * 24 * 60 * 60 * 1000) // 30 days
            Urgency.WHEN_CONVENIENT -> currentTime + (90L * 24 * 60 * 60 * 1000) // 90 days
        }
    }

    private fun getDefaultSafetyAssessment(): SafetyAssessment {
        return SafetyAssessment(
            overallRiskLevel = RiskLevel.MODERATE,
            personalizedRecommendations = getGeneralAdultRecommendations(),
            threatSummary = ThreatAnalysis(
                activeThreatCount = 2,
                primaryThreats = listOf(ThreatType.MPESA_SCAM, ThreatType.PHISHING_EMAIL),
                trendingScams = listOf("M-Pesa reversal scams", "Fake job offers"),
                personalRiskFactors = listOf("Unable to assess user profile"),
                environmentalRisks = listOf("General cybersecurity risks")
            ),
            actionItems = emptyList(),
            confidenceScore = 0.3f
        )
    }

    private fun getGeneralAdultRecommendations(): List<SafetyRecommendation> {
        return listOf(
            SafetyRecommendation(
                category = RecommendationCategory.DEVICE_SECURITY,
                title = "Enable Screen Lock",
                description = "Set up PIN, pattern, or biometric lock on your device",
                priority = Priority.HIGH,
                actionRequired = true,
                estimatedTime = "2 minutes",
                benefits = listOf("Prevent unauthorized access", "Protect personal data", "Secure banking apps")
            )
        )
    }

    private fun getBeginnerRecommendations(): List<SafetyRecommendation> = emptyList()
    private fun getIntermediateRecommendations(): List<SafetyRecommendation> = emptyList()
    private fun getAdvancedRecommendations(): List<SafetyRecommendation> = emptyList()
}
