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
import java.util.*

/**
 * Enhanced Cybercrime Reporting System
 * Handles various types of cybercrime with specialized templates and processing
 */
class CybercrimeReportingSystem(private val context: Context) {

    companion object {
        private const val TAG = "CybercrimeReporting"
    }

    enum class CybercrimeType {
        MPESA_SCAM,
        PHISHING_EMAIL,
        FAKE_WEBSITE,
        SOCIAL_MEDIA_HARASSMENT,
        IDENTITY_THEFT,
        ROMANCE_SCAM,
        JOB_SCAM,
        CYBERBULLYING,
        REVENGE_PORN,
        CHILD_EXPLOITATION,
        HATE_SPEECH,
        DATA_BREACH,
        RANSOMWARE,
        OTHER
    }

    data class CybercrimeReport(
        val id: String = UUID.randomUUID().toString(),
        val type: CybercrimeType,
        val title: String,
        val description: String,
        val evidenceIds: List<String> = emptyList(),
        val platformsInvolved: List<String> = emptyList(),
        val suspectedPerpetrator: PerpetratorInfo? = null,
        val victimInfo: VictimInfo,
        val incidentTimestamp: Long,
        val reportTimestamp: Long = System.currentTimeMillis(),
        val severity: SeverityLevel,
        val isAnonymous: Boolean = false,
        val location: String? = null,
        val additionalMetadata: Map<String, String> = emptyMap()
    )

    data class PerpetratorInfo(
        val knownIdentifier: String? = null, // Phone, email, username, etc.
        val platform: String? = null,
        val additionalInfo: String? = null
    )

    data class VictimInfo(
        val ageGroup: AgeGroup,
        val gender: Gender? = null,
        val isReportingForSelf: Boolean = true,
        val relationshipToPerpetrator: String? = null
    )

    enum class AgeGroup {
        UNDER_18, ADULT_18_30, ADULT_31_50, ADULT_51_PLUS, PREFER_NOT_TO_SAY
    }

    enum class Gender {
        MALE, FEMALE, NON_BINARY, PREFER_NOT_TO_SAY
    }

    enum class SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Create a quick report template based on cybercrime type
     */
    fun createQuickReportTemplate(type: CybercrimeType): ReportTemplate {
        return when (type) {
            CybercrimeType.MPESA_SCAM -> createMpesaScamTemplate()
            CybercrimeType.PHISHING_EMAIL -> createPhishingTemplate()
            CybercrimeType.FAKE_WEBSITE -> createFakeWebsiteTemplate()
            CybercrimeType.SOCIAL_MEDIA_HARASSMENT -> createSocialMediaHarassmentTemplate()
            CybercrimeType.ROMANCE_SCAM -> createRomanceScamTemplate()
            CybercrimeType.JOB_SCAM -> createJobScamTemplate()
            CybercrimeType.CYBERBULLYING -> createCyberbullyingTemplate()
            else -> createGenericTemplate(type)
        }
    }

    data class ReportTemplate(
        val type: CybercrimeType,
        val title: String,
        val guidedQuestions: List<GuidedQuestion>,
        val requiredEvidence: List<String>,
        val suggestedActions: List<String>,
        val relevantAuthorities: List<String>
    )

    data class GuidedQuestion(
        val question: String,
        val type: QuestionType,
        val options: List<String>? = null,
        val isRequired: Boolean = true
    )

    enum class QuestionType {
        TEXT, MULTIPLE_CHOICE, DATE, TIME, PHONE_NUMBER, EMAIL, URL
    }

    private fun createMpesaScamTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.MPESA_SCAM,
            title = "M-Pesa/Mobile Money Scam Report",
            guidedQuestions = listOf(
                GuidedQuestion("What type of M-Pesa scam was this?", QuestionType.MULTIPLE_CHOICE, 
                    listOf("Fake transaction confirmation", "PIN request", "Reversal scam", "Lottery/prize scam", "Other")),
                GuidedQuestion("What was the sender's number?", QuestionType.PHONE_NUMBER),
                GuidedQuestion("What was the exact message content?", QuestionType.TEXT),
                GuidedQuestion("When did you receive this message?", QuestionType.DATE),
                GuidedQuestion("Did you respond or take any action?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No action taken", "Replied to message", "Called the number", "Shared PIN", "Sent money")),
                GuidedQuestion("Have you reported this to Safaricom?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Yes", "No", "Planning to"))
            ),
            requiredEvidence = listOf("Screenshot of SMS", "Call logs (if applicable)"),
            suggestedActions = listOf(
                "Block the sender number",
                "Report to Safaricom: 0722000000",
                "If money was lost, visit nearest Safaricom shop",
                "Change M-Pesa PIN if compromised"
            ),
            relevantAuthorities = listOf("Safaricom Customer Care", "DCI Cybercrime Unit", "Communications Authority of Kenya")
        )
    }

    private fun createPhishingTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.PHISHING_EMAIL,
            title = "Phishing Email/Message Report",
            guidedQuestions = listOf(
                GuidedQuestion("Where did you receive the phishing attempt?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Email", "SMS", "WhatsApp", "Social Media", "Other")),
                GuidedQuestion("What did the message claim to be from?", QuestionType.TEXT),
                GuidedQuestion("What information were they requesting?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Login credentials", "Banking details", "Personal information", "PIN/Password", "Other")),
                GuidedQuestion("Did you click any links or download attachments?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No", "Clicked link", "Downloaded file", "Both")),
                GuidedQuestion("Did you provide any information?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No information given", "Partial information", "Full information requested"))
            ),
            requiredEvidence = listOf("Screenshot of message", "Email headers", "Suspicious URLs"),
            suggestedActions = listOf(
                "Do not click suspicious links",
                "Change passwords if compromised",
                "Enable two-factor authentication",
                "Scan device for malware"
            ),
            relevantAuthorities = listOf("DCI Cybercrime Unit", "Your bank (if financial)", "Platform administrators")
        )
    }

    private fun createSocialMediaHarassmentTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.SOCIAL_MEDIA_HARASSMENT,
            title = "Social Media Harassment Report",
            guidedQuestions = listOf(
                GuidedQuestion("Which platform did this occur on?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Facebook", "Twitter", "Instagram", "TikTok", "WhatsApp", "Telegram", "Other")),
                GuidedQuestion("What type of harassment?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Threats", "Bullying", "Stalking", "Impersonation", "Hate speech", "Sexual harassment")),
                GuidedQuestion("Do you know the perpetrator?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Complete stranger", "Acquaintance", "Former friend/partner", "Known person")),
                GuidedQuestion("How long has this been happening?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Single incident", "Few days", "Weeks", "Months", "Over a year")),
                GuidedQuestion("Have you reported to the platform?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Yes", "No", "Planning to"))
            ),
            requiredEvidence = listOf("Screenshots of harassment", "Profile information", "Message history"),
            suggestedActions = listOf(
                "Block the harasser",
                "Report to platform administrators",
                "Document all incidents",
                "Consider privacy settings adjustment"
            ),
            relevantAuthorities = listOf("Platform support", "DCI Cybercrime Unit", "Gender-based violence helplines")
        )
    }

    private fun createRomanceScamTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.ROMANCE_SCAM,
            title = "Romance/Dating Scam Report",
            guidedQuestions = listOf(
                GuidedQuestion("Where did you meet this person?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Dating app", "Social media", "Email", "Gaming platform", "Other")),
                GuidedQuestion("How long were you in contact?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Days", "Weeks", "Months", "Over a year")),
                GuidedQuestion("Did they ask for money?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No", "Yes - for travel", "Yes - for emergency", "Yes - for business", "Yes - other reason")),
                GuidedQuestion("Did you send money or gifts?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No", "Small amounts", "Significant amounts", "Multiple payments")),
                GuidedQuestion("What made you realize it was a scam?", QuestionType.TEXT)
            ),
            requiredEvidence = listOf("Chat conversations", "Photos shared", "Payment receipts", "Profile screenshots"),
            suggestedActions = listOf(
                "Stop all communication",
                "Report to platform",
                "Contact bank if money was sent",
                "Seek emotional support"
            ),
            relevantAuthorities = listOf("DCI Cybercrime Unit", "Your bank", "Platform administrators")
        )
    }

    private fun createJobScamTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.JOB_SCAM,
            title = "Employment/Job Scam Report",
            guidedQuestions = listOf(
                GuidedQuestion("Where did you find this job offer?", QuestionType.MULTIPLE_CHOICE,
                    listOf("WhatsApp", "Email", "Job website", "Social media", "SMS", "Phone call")),
                GuidedQuestion("What type of job scam?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Upfront fee request", "Fake company", "Work from home scam", "Pyramid scheme", "Other")),
                GuidedQuestion("Did they request money or personal information?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Money for registration", "Personal documents", "Bank details", "All of the above", "None")),
                GuidedQuestion("Did you pay any money?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No payment made", "Small registration fee", "Significant amount", "Multiple payments")),
            ),
            requiredEvidence = listOf("Job advertisement", "Communication records", "Payment receipts", "Company details"),
            suggestedActions = listOf(
                "Do not make further payments",
                "Research company legitimacy",
                "Report to job platform",
                "Warn others about the scam"
            ),
            relevantAuthorities = listOf("DCI Cybercrime Unit", "Ministry of Labour", "Job platform administrators")
        )
    }

    private fun createCyberbullyingTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.CYBERBULLYING,
            title = "Cyberbullying Report",
            guidedQuestions = listOf(
                GuidedQuestion("Who is the target of bullying?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Myself", "My child", "Someone I know", "Other")),
                GuidedQuestion("What platforms is this happening on?", QuestionType.MULTIPLE_CHOICE,
                    listOf("WhatsApp groups", "Social media", "Gaming platforms", "School systems", "Multiple platforms")),
                GuidedQuestion("What type of bullying behavior?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Name calling", "Threats", "Exclusion", "Spreading rumors", "Sharing private content")),
                GuidedQuestion("Is this affecting school or work?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Yes, significantly", "Somewhat", "Not yet", "Not applicable"))
            ),
            requiredEvidence = listOf("Screenshots of bullying", "Group chat records", "Witness statements"),
            suggestedActions = listOf(
                "Document all incidents",
                "Report to platform",
                "Inform school/workplace if relevant",
                "Seek counseling support"
            ),
            relevantAuthorities = listOf("School administration", "Platform support", "DCI Cybercrime Unit", "Counseling services")
        )
    }

    private fun createFakeWebsiteTemplate(): ReportTemplate {
        return ReportTemplate(
            type = CybercrimeType.FAKE_WEBSITE,
            title = "Fake Website/Online Store Report",
            guidedQuestions = listOf(
                GuidedQuestion("What type of fake website?", QuestionType.MULTIPLE_CHOICE,
                    listOf("Fake online store", "Fake bank website", "Fake government site", "Fake social media", "Other")),
                GuidedQuestion("How did you discover it was fake?", QuestionType.TEXT),
                GuidedQuestion("Did you provide any information?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No information", "Personal details", "Payment information", "Login credentials")),
                GuidedQuestion("Did you make any payments?", QuestionType.MULTIPLE_CHOICE,
                    listOf("No payment", "Small amount", "Significant purchase", "Multiple transactions"))
            ),
            requiredEvidence = listOf("Website URL", "Screenshots", "Payment receipts", "Email confirmations"),
            suggestedActions = listOf(
                "Stop using the website",
                "Contact bank if payments made",
                "Change passwords if compromised",
                "Report to web hosting provider"
            ),
            relevantAuthorities = listOf("DCI Cybercrime Unit", "Communications Authority", "Your bank")
        )
    }

    private fun createGenericTemplate(type: CybercrimeType): ReportTemplate {
        return ReportTemplate(
            type = type,
            title = "${type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} Report",
            guidedQuestions = listOf(
                GuidedQuestion("Please describe what happened", QuestionType.TEXT),
                GuidedQuestion("When did this incident occur?", QuestionType.DATE),
                GuidedQuestion("What platforms or services were involved?", QuestionType.TEXT),
                GuidedQuestion("Do you know who is responsible?", QuestionType.TEXT, isRequired = false)
            ),
            requiredEvidence = listOf("Screenshots", "Documentation", "Communication records"),
            suggestedActions = listOf(
                "Document the incident",
                "Change passwords if necessary",
                "Report to relevant platforms"
            ),
            relevantAuthorities = listOf("DCI Cybercrime Unit", "Relevant platform administrators")
        )
    }

    /**
     * Submit a cybercrime report
     */
    fun submitReport(report: CybercrimeReport): Result<String> {
        return try {
            // Validate report
            if (!validateReport(report)) {
                return Result.failure(IllegalArgumentException("Invalid report data"))
            }

            // Process evidence
            val evidenceManager = EvidenceManager(context)
            report.evidenceIds.forEach { evidenceId ->
                // Add custody entry for report submission
                Log.d(TAG, "Processing evidence: $evidenceId")
            }

            // Store report securely
            // In production, this would integrate with Firebase/backend
            storeReport(report)

            // Generate report reference
            val reportReference = generateReportReference(report)

            // Send to relevant authorities based on type
            routeToAuthorities(report)

            Log.i(TAG, "Cybercrime report submitted successfully: $reportReference")
            Result.success(reportReference)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit cybercrime report", e)
            Result.failure(e)
        }
    }

    private fun validateReport(report: CybercrimeReport): Boolean {
        return report.title.isNotBlank() && 
               report.description.isNotBlank() &&
               report.incidentTimestamp > 0
    }

    private fun storeReport(report: CybercrimeReport) {
        // Implementation would store to secure database
        Log.d(TAG, "Storing report: ${report.id}")
    }

    private fun generateReportReference(report: CybercrimeReport): String {
        val prefix = when (report.type) {
            CybercrimeType.MPESA_SCAM -> "MPS"
            CybercrimeType.PHISHING_EMAIL -> "PHI"
            CybercrimeType.SOCIAL_MEDIA_HARASSMENT -> "SMH"
            CybercrimeType.ROMANCE_SCAM -> "ROM"
            CybercrimeType.JOB_SCAM -> "JOB"
            else -> "CYB"
        }
        
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "$prefix-$timestamp"
    }

    private fun routeToAuthorities(report: CybercrimeReport) {
        // Route to appropriate authorities based on report type
        when (report.type) {
            CybercrimeType.MPESA_SCAM -> {
                // Route to Safaricom and DCI
                Log.d(TAG, "Routing M-Pesa scam to Safaricom and DCI")
            }
            CybercrimeType.CHILD_EXPLOITATION -> {
                // Priority routing to child protection services
                Log.d(TAG, "Priority routing for child exploitation case")
            }
            else -> {
                // Standard routing to DCI Cybercrime Unit
                Log.d(TAG, "Standard routing to DCI Cybercrime Unit")
            }
        }
    }
}
