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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.safenet.shield.R
import com.safenet.shield.ai.DigitalSafetyAssistant
import com.safenet.shield.analytics.SecurityAnalyticsDashboard
import com.safenet.shield.community.CommunityIntelligenceSystem
import com.safenet.shield.ml.MLThreatPredictor
import kotlinx.coroutines.launch

/**
 * Advanced Safety Dashboard Fragment
 * Main UI for displaying AI insights, threat predictions, and community intelligence
 */
class AdvancedSafetyDashboardFragment : Fragment() {

    private lateinit var safetyAssistant: DigitalSafetyAssistant
    private lateinit var analyticsSystem: SecurityAnalyticsDashboard
    private lateinit var communityIntelligence: CommunityIntelligenceSystem
    private lateinit var mlPredictor: MLThreatPredictor
    
    private lateinit var dashboardViewModel: AdvancedDashboardViewModel
    
    // UI Components
    private lateinit var securityScoreCard: MaterialCardView
    private lateinit var securityScoreProgress: CircularProgressIndicator
    private lateinit var threatLevelIndicator: LinearProgressIndicator
    private lateinit var safetyRecommendationsRecycler: RecyclerView
    private lateinit var communityAlertsRecycler: RecyclerView
    private lateinit var threatPredictionsRecycler: RecyclerView
    private lateinit var panicButton: FloatingActionButton
    private lateinit var riskLevelChips: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_advanced_safety_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        initializeUI(view)
        setupObservers()
        loadDashboardData()
    }

    private fun initializeComponents() {
        safetyAssistant = DigitalSafetyAssistant(requireContext())
        analyticsSystem = SecurityAnalyticsDashboard(requireContext())
        communityIntelligence = CommunityIntelligenceSystem(requireContext())
        mlPredictor = MLThreatPredictor(requireContext())
        
        dashboardViewModel = ViewModelProvider(this)[AdvancedDashboardViewModel::class.java]
    }

    private fun initializeUI(view: View) {
        // Security Score Card
        securityScoreCard = view.findViewById(R.id.security_score_card)
        securityScoreProgress = view.findViewById(R.id.security_score_progress)
        threatLevelIndicator = view.findViewById(R.id.threat_level_indicator)
        
        // RecyclerViews
        safetyRecommendationsRecycler = view.findViewById(R.id.safety_recommendations_recycler)
        communityAlertsRecycler = view.findViewById(R.id.community_alerts_recycler)
        threatPredictionsRecycler = view.findViewById(R.id.threat_predictions_recycler)
        
        // Action Button
        panicButton = view.findViewById(R.id.panic_button)
        
        // Risk Level Chips
        riskLevelChips = view.findViewById(R.id.risk_level_chips)
        
        setupRecyclerViews()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        safetyRecommendationsRecycler.layoutManager = LinearLayoutManager(context)
        communityAlertsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        threatPredictionsRecycler.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        panicButton.setOnClickListener {
            triggerEmergencyAlert()
        }
        
        securityScoreCard.setOnClickListener {
            // Navigate to detailed analytics
            // findNavController().navigate(R.id.action_to_analytics_detail)
        }
    }

    private fun setupObservers() {
        dashboardViewModel.securityScore.observe(viewLifecycleOwner) { score ->
            updateSecurityScore(score)
        }
        
        dashboardViewModel.threatLevel.observe(viewLifecycleOwner) { level ->
            updateThreatLevel(level)
        }
        
        dashboardViewModel.safetyRecommendations.observe(viewLifecycleOwner) { recommendations ->
            updateSafetyRecommendations(recommendations)
        }
        
        dashboardViewModel.communityAlerts.observe(viewLifecycleOwner) { alerts ->
            updateCommunityAlerts(alerts)
        }
        
        dashboardViewModel.threatPredictions.observe(viewLifecycleOwner) { predictions ->
            updateThreatPredictions(predictions)
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                // Initialize ML system
                mlPredictor.initializeMLSystem()
                
                // Generate safety assessment
                val userProfile = createUserProfile()
                val safetyAssessment = safetyAssistant.generateSafetyAssessment(userProfile)
                dashboardViewModel.updateSafetyAssessment(safetyAssessment)
                
                // Load analytics dashboard
                val analyticsResult = analyticsSystem.generateSecurityDashboard()
                analyticsResult.getOrNull()?.let { dashboard ->
                    dashboardViewModel.updateAnalyticsDashboard(dashboard)
                }
                
                // Load community intelligence
                val mockLocation = android.location.Location("").apply {
                    latitude = 0.0
                    longitude = 0.0
                }
                val communityAlerts = communityIntelligence.getNearbyAlerts(mockLocation)
                communityAlerts.getOrNull()?.let { alerts ->
                    dashboardViewModel.updateCommunityAlerts(alerts)
                }
                
                // Generate threat predictions
                val contextData = createThreatContextData()
                val predictions = mlPredictor.generateThreatPredictions(contextData)
                predictions.getOrNull()?.let { threatList ->
                    dashboardViewModel.updateThreatPredictions(threatList)
                }
                
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading dashboard data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSecurityScore(score: Int) {
        securityScoreProgress.setProgressCompat(score, true)
        
        // Update risk level chips
        riskLevelChips.removeAllViews()
        val riskLevel = when {
            score >= 90 -> "Excellent"
            score >= 75 -> "Good"
            score >= 60 -> "Moderate"
            score >= 40 -> "Poor"
            else -> "Critical"
        }
        
        val chip = Chip(requireContext()).apply {
            text = riskLevel
            chipBackgroundColor = resources.getColorStateList(
                when (riskLevel) {
                    "Excellent" -> R.color.green_500
                    "Good" -> R.color.blue_500
                    "Moderate" -> R.color.orange_500
                    "Poor" -> R.color.red_300
                    else -> R.color.red_500
                }, null
            )
        }
        riskLevelChips.addView(chip)
    }

    private fun updateThreatLevel(level: Float) {
        threatLevelIndicator.setProgressCompat((level * 100).toInt(), true)
    }

    private fun updateSafetyRecommendations(recommendations: List<DigitalSafetyAssistant.SafetyRecommendation>) {
        val adapter = SafetyRecommendationsAdapter(recommendations) { recommendation ->
            // Handle recommendation click
            showRecommendationDetail(recommendation)
        }
        safetyRecommendationsRecycler.adapter = adapter
    }

    private fun updateCommunityAlerts(alerts: List<CommunityIntelligenceSystem.SafetyAlert>) {
        val adapter = CommunityAlertsAdapter(alerts) { alert ->
            // Handle alert click
            showAlertDetail(alert)
        }
        communityAlertsRecycler.adapter = adapter
    }

    private fun updateThreatPredictions(predictions: List<MLThreatPredictor.ThreatPrediction>) {
        val adapter = ThreatPredictionsAdapter(predictions) { prediction ->
            // Handle prediction click
            showPredictionDetail(prediction)
        }
        threatPredictionsRecycler.adapter = adapter
    }

    private fun triggerEmergencyAlert() {
        lifecycleScope.launch {
            try {
                // This would integrate with OfflineEmergencyManager
                Toast.makeText(context, "Emergency alert triggered!", Toast.LENGTH_SHORT).show()
                
                // Animate panic button
                panicButton.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction {
                        panicButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                    }
                
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to trigger emergency alert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserProfile(): DigitalSafetyAssistant.UserSafetyProfile {
        return DigitalSafetyAssistant.UserSafetyProfile(
            ageGroup = DigitalSafetyAssistant.AgeGroup.ADULT_26_40,
            techSavviness = DigitalSafetyAssistant.TechLevel.INTERMEDIATE,
            primaryPlatforms = listOf("Android", "WhatsApp", "M-Pesa"),
            financialActivity = DigitalSafetyAssistant.FinancialActivity.MOBILE_MONEY_USER,
            locationRisk = DigitalSafetyAssistant.LocationRisk.URBAN_MODERATE,
            previousIncidents = emptyList(),
            securityMeasuresEnabled = listOf("Screen Lock", "App Permissions"),
            lastSecurityUpdate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        )
    }

    private fun createThreatContextData(): MLThreatPredictor.ThreatContextData {
        return MLThreatPredictor.ThreatContextData(
            userLocation = null, // Would get from location service
            timeOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK),
            recentActivity = listOf("SMS", "Call", "M-Pesa"),
            communicationPatterns = listOf("WhatsApp", "SMS", "Email"),
            financialActivity = listOf("M-Pesa transfer", "Bank check"),
            socialMediaUsage = listOf("WhatsApp", "Facebook"),
            deviceInfo = mapOf("model" to "Android", "version" to "13"),
            networkInfo = mapOf("type" to "WiFi", "carrier" to "Safaricom"),
            communityAlerts = emptyList()
        )
    }

    private fun showRecommendationDetail(recommendation: DigitalSafetyAssistant.SafetyRecommendation) {
        // Show detailed recommendation dialog or navigate to detail fragment
        Toast.makeText(context, "Recommendation: ${recommendation.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showAlertDetail(alert: CommunityIntelligenceSystem.SafetyAlert) {
        // Show detailed alert dialog or navigate to detail fragment
        Toast.makeText(context, "Alert: ${alert.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showPredictionDetail(prediction: MLThreatPredictor.ThreatPrediction) {
        // Show detailed prediction dialog or navigate to detail fragment
        Toast.makeText(context, "Threat: ${prediction.threatType}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mlPredictor.cleanup()
    }
}
