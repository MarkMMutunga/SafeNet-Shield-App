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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.safenet.shield.R
import com.safenet.shield.ai.DigitalSafetyAssistant
import com.safenet.shield.community.CommunityIntelligenceSystem
import com.safenet.shield.ml.MLThreatPredictor

/**
 * Adapter for Safety Recommendations RecyclerView
 */
class SafetyRecommendationsAdapter(
    private val recommendations: List<DigitalSafetyAssistant.SafetyRecommendation>,
    private val onItemClick: (DigitalSafetyAssistant.SafetyRecommendation) -> Unit
) : RecyclerView.Adapter<SafetyRecommendationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val priorityIcon: ImageView = view.findViewById(R.id.priority_icon)
        val titleText: TextView = view.findViewById(R.id.title_text)
        val descriptionText: TextView = view.findViewById(R.id.description_text)
        val timeEstimate: TextView = view.findViewById(R.id.time_estimate)
        val categoryChip: TextView = view.findViewById(R.id.category_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_safety_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]
        
        holder.titleText.text = recommendation.title
        holder.descriptionText.text = recommendation.description
        holder.timeEstimate.text = recommendation.estimatedTime
        holder.categoryChip.text = recommendation.category.name.replace("_", " ")
        
        // Set priority icon and color
        val (iconRes, colorRes) = when (recommendation.priority) {
            DigitalSafetyAssistant.Priority.URGENT -> R.drawable.ic_priority_high to R.color.red_500
            DigitalSafetyAssistant.Priority.HIGH -> R.drawable.ic_arrow_upward to R.color.orange_500
            DigitalSafetyAssistant.Priority.MEDIUM -> R.drawable.ic_remove to R.color.blue_500
            DigitalSafetyAssistant.Priority.LOW -> R.drawable.ic_arrow_downward to R.color.green_500
        }
        
        holder.priorityIcon.setImageResource(iconRes)
        holder.priorityIcon.setColorFilter(
            holder.itemView.context.getColor(colorRes)
        )
        
        holder.itemView.setOnClickListener { onItemClick(recommendation) }
    }

    override fun getItemCount() = recommendations.size
}

/**
 * Adapter for Community Alerts RecyclerView
 */
class CommunityAlertsAdapter(
    private val alerts: List<CommunityIntelligenceSystem.SafetyAlert>,
    private val onItemClick: (CommunityIntelligenceSystem.SafetyAlert) -> Unit
) : RecyclerView.Adapter<CommunityAlertsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val severityIndicator: View = view.findViewById(R.id.severity_indicator)
        val titleText: TextView = view.findViewById(R.id.title_text)
        val locationText: TextView = view.findViewById(R.id.location_text)
        val timeText: TextView = view.findViewById(R.id.time_text)
        val statusChip: TextView = view.findViewById(R.id.status_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_alert, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = alerts[position]
        
        holder.titleText.text = alert.title
        holder.locationText.text = alert.location?.let { "${it.latitude}, ${it.longitude}" } ?: "Location unknown"
        holder.timeText.text = formatTime(alert.timestamp)
        holder.statusChip.text = if (alert.isVerified) "Verified" else "Unverified"
        
        // Set severity indicator color
        val severityColor = when (alert.severity) {
            CommunityIntelligenceSystem.AlertSeverity.CRITICAL -> R.color.red_500
            CommunityIntelligenceSystem.AlertSeverity.HIGH -> R.color.orange_500
            CommunityIntelligenceSystem.AlertSeverity.MEDIUM -> R.color.yellow_500
            CommunityIntelligenceSystem.AlertSeverity.LOW -> R.color.blue_500
        }
        
        holder.severityIndicator.setBackgroundColor(
            holder.itemView.context.getColor(severityColor)
        )
        
        holder.itemView.setOnClickListener { onItemClick(alert) }
    }

    override fun getItemCount() = alerts.size

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        }
    }
}

/**
 * Adapter for Threat Predictions RecyclerView
 */
class ThreatPredictionsAdapter(
    private val predictions: List<MLThreatPredictor.ThreatPrediction>,
    private val onItemClick: (MLThreatPredictor.ThreatPrediction) -> Unit
) : RecyclerView.Adapter<ThreatPredictionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val threatIcon: ImageView = view.findViewById(R.id.threat_icon)
        val threatTypeText: TextView = view.findViewById(R.id.threat_type_text)
        val probabilityText: TextView = view.findViewById(R.id.probability_text)
        val riskLevelChip: TextView = view.findViewById(R.id.risk_level_chip)
        val timeWindowText: TextView = view.findViewById(R.id.time_window_text)
        val confidenceBar: View = view.findViewById(R.id.confidence_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_threat_prediction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prediction = predictions[position]
        
        holder.threatTypeText.text = prediction.threatType.name.replace("_", " ")
        holder.probabilityText.text = "${(prediction.probability * 100).toInt()}%"
        holder.riskLevelChip.text = prediction.riskLevel.name
        holder.timeWindowText.text = prediction.timeWindow.name.replace("_", " ")
        
        // Set threat icon based on type
        val iconRes = when (prediction.threatType) {
            MLThreatPredictor.ThreatType.MPESA_SCAM -> R.drawable.ic_money_off
            MLThreatPredictor.ThreatType.PHISHING_ATTACK -> R.drawable.ic_phishing
            MLThreatPredictor.ThreatType.IDENTITY_THEFT -> R.drawable.ic_person_off
            MLThreatPredictor.ThreatType.ROMANCE_SCAM -> R.drawable.ic_favorite_border
            MLThreatPredictor.ThreatType.INVESTMENT_FRAUD -> R.drawable.ic_trending_down
            MLThreatPredictor.ThreatType.CYBERBULLYING -> R.drawable.ic_forum
            MLThreatPredictor.ThreatType.ACCOUNT_TAKEOVER -> R.drawable.ic_account_circle
            MLThreatPredictor.ThreatType.SOCIAL_ENGINEERING -> R.drawable.ic_psychology
            MLThreatPredictor.ThreatType.RANSOMWARE -> R.drawable.ic_lock
            MLThreatPredictor.ThreatType.FAKE_NEWS_MISINFORMATION -> R.drawable.ic_fake_news
        }
        
        holder.threatIcon.setImageResource(iconRes)
        
        // Set risk level chip color
        val chipColor = when (prediction.riskLevel) {
            MLThreatPredictor.RiskLevel.EXTREME -> R.color.red_700
            MLThreatPredictor.RiskLevel.CRITICAL -> R.color.red_500
            MLThreatPredictor.RiskLevel.HIGH -> R.color.orange_500
            MLThreatPredictor.RiskLevel.MODERATE -> R.color.yellow_500
            MLThreatPredictor.RiskLevel.LOW -> R.color.blue_500
            MLThreatPredictor.RiskLevel.VERY_LOW -> R.color.green_500
        }
        
        holder.riskLevelChip.setBackgroundColor(
            holder.itemView.context.getColor(chipColor)
        )
        
        // Set confidence bar width
        val confidenceWidth = (prediction.confidence * 100).toInt()
        val layoutParams = holder.confidenceBar.layoutParams
        layoutParams.width = (holder.itemView.width * confidenceWidth / 100)
        holder.confidenceBar.layoutParams = layoutParams
        
        holder.itemView.setOnClickListener { onItemClick(prediction) }
    }

    override fun getItemCount() = predictions.size
}
