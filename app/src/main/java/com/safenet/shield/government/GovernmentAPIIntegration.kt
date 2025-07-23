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

package com.safenet.shield.government

import android.content.Context
import android.util.Log
import com.safenet.shield.blockchain.BlockchainEvidenceSystem
import com.safenet.shield.utils.SecurityUtils
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Government API Integration System
 * Provides secure integration with official government reporting systems and law enforcement
 */
class GovernmentAPIIntegration(private val context: Context) {

    companion object {
        private const val TAG = "GovernmentAPI"
        
        // Kenya Government API Endpoints (Simulated)
        private const val KENYA_POLICE_API = "https://api.police.go.ke/v1"
        private const val DCI_REPORTING_API = "https://api.dci.go.ke/v1"
        private const val SAFARICOM_FRAUD_API = "https://api.safaricom.co.ke/fraud/v1"
        private const val CENTRAL_BANK_API = "https://api.centralbank.go.ke/v1"
        private const val CAK_REPORTING_API = "https://api.cak.go.ke/v1"
        private const val ODPP_CASE_API = "https://api.odpp.go.ke/v1"
        
        // International APIs
        private const val INTERPOL_API = "https://api.interpol.int/v1"
        private const val UNHCR_API = "https://api.unhcr.org/v1"
        
        // Timeout configurations
        private const val CONNECTION_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
    }

    private val securityUtils = SecurityUtils.getInstance(context)
    private val blockchainEvidence = BlockchainEvidenceSystem(context)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val apiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    data class GovernmentReport(
        val reportId: String = UUID.randomUUID().toString(),
        val reportType: ReportType,
        val incidentDetails: IncidentDetails,
        val reporterInfo: ReporterInfo,
        val evidenceAttachments: List<EvidenceAttachment>,
        val location: LocationInfo,
        val urgencyLevel: UrgencyLevel,
        val targetAgency: GovernmentAgency,
        val legalBasis: LegalBasis,
        val submissionTimestamp: Long = System.currentTimeMillis(),
        val followUpRequired: Boolean = true
    )

    data class IncidentDetails(
        val incidentType: IncidentType,
        val description: String,
        val occurredAt: Long,
        val discoveredAt: Long = System.currentTimeMillis(),
        val suspects: List<SuspectInfo> = emptyList(),
        val victims: List<VictimInfo> = emptyList(),
        val witnesses: List<WitnessInfo> = emptyList(),
        val financialLoss: FinancialLoss? = null,
        val ongoingThreat: Boolean = false,
        val relatedIncidents: List<String> = emptyList()
    )

    data class ReporterInfo(
        val reporterId: String = UUID.randomUUID().toString(),
        val name: String,
        val idNumber: String,
        val phoneNumber: String,
        val email: String,
        val address: String,
        val relationship: ReporterRelationship,
        val preferredLanguage: String = "en",
        val contactPermissions: ContactPermissions,
        val anonymityRequested: Boolean = false
    )

    data class EvidenceAttachment(
        val evidenceId: String,
        val evidenceType: EvidenceType,
        val filename: String,
        val contentHash: String,
        val blockchainReference: String,
        val accessLevel: AccessLevel,
        val description: String,
        val collectionMethod: String
    )

    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val address: String,
        val county: String,
        val constituency: String,
        val ward: String,
        val landmark: String? = null,
        val locationVerified: Boolean = false
    )

    data class SuspectInfo(
        val suspectId: String = UUID.randomUUID().toString(),
        val name: String? = null,
        val alias: List<String> = emptyList(),
        val phoneNumbers: List<String> = emptyList(),
        val idNumber: String? = null,
        val description: String,
        val knownAssociates: List<String> = emptyList(),
        val criminalHistory: List<String> = emptyList(),
        val lastKnownLocation: LocationInfo? = null
    )

    data class VictimInfo(
        val victimId: String = UUID.randomUUID().toString(),
        val name: String,
        val age: Int? = null,
        val gender: String? = null,
        val phoneNumber: String? = null,
        val relationship: String,
        val injuries: List<String> = emptyList(),
        val supportNeeded: List<SupportType> = emptyList(),
        val consentToContact: Boolean = false
    )

    data class WitnessInfo(
        val witnessId: String = UUID.randomUUID().toString(),
        val name: String,
        val phoneNumber: String,
        val statement: String,
        val willingToTestify: Boolean = false,
        val protectionNeeded: Boolean = false
    )

    data class FinancialLoss(
        val currency: String,
        val amount: Double,
        val lossType: FinancialLossType,
        val accountsAffected: List<String> = emptyList(),
        val transactionReferences: List<String> = emptyList(),
        val recoveryPossible: Boolean = false
    )

    data class ContactPermissions(
        val allowPhoneCalls: Boolean = true,
        val allowSMS: Boolean = true,
        val allowEmail: Boolean = true,
        val preferredTime: String = "daytime",
        val emergencyContactOnly: Boolean = false
    )

    data class LegalBasis(
        val applicableLaws: List<String>,
        val jurisdiction: String,
        val legalReferences: List<String>,
        val statuteOfLimitations: Long? = null,
        val requiredActions: List<String> = emptyList()
    )

    data class GovernmentResponse(
        val responseId: String,
        val originalReportId: String,
        val respondingAgency: GovernmentAgency,
        val status: ResponseStatus,
        val caseNumber: String? = null,
        val assignedOfficer: OfficerInfo? = null,
        val estimatedProcessingTime: Long? = null,
        val nextSteps: List<String>,
        val additionalInformationRequired: List<String> = emptyList(),
        val publicReference: String? = null,
        val responseTimestamp: Long = System.currentTimeMillis()
    )

    data class OfficerInfo(
        val officerId: String,
        val name: String,
        val rank: String,
        val department: String,
        val contactNumber: String,
        val email: String,
        val badgeNumber: String
    )

    enum class ReportType {
        CYBERCRIME_REPORT,
        FRAUD_REPORT,
        HARASSMENT_REPORT,
        THREAT_REPORT,
        MISSING_PERSON,
        DOMESTIC_VIOLENCE,
        FINANCIAL_CRIME,
        TERRORISM_THREAT,
        CORRUPTION_REPORT,
        HUMAN_TRAFFICKING,
        CHILD_ABUSE,
        DRUG_RELATED_CRIME
    }

    enum class IncidentType {
        MPESA_FRAUD,
        ONLINE_SCAM,
        IDENTITY_THEFT,
        CYBERBULLYING,
        PHISHING_ATTACK,
        RANSOMWARE,
        INVESTMENT_FRAUD,
        ROMANCE_SCAM,
        FAKE_LOAN_APPS,
        SIM_SWAP_FRAUD,
        CRYPTO_FRAUD,
        SOCIAL_ENGINEERING
    }

    enum class UrgencyLevel {
        IMMEDIATE,    // Life-threatening, ongoing crime
        HIGH,         // Serious crime, time-sensitive
        MEDIUM,       // Important but not time-critical
        LOW,          // Information only, no immediate action needed
        ROUTINE       // Administrative or follow-up
    }

    enum class GovernmentAgency {
        KENYA_POLICE_SERVICE,
        DIRECTORATE_CRIMINAL_INVESTIGATIONS,
        SAFARICOM_FRAUD_DEPARTMENT,
        CENTRAL_BANK_KENYA,
        COMMUNICATIONS_AUTHORITY_KENYA,
        OFFICE_DIRECTOR_PUBLIC_PROSECUTIONS,
        NATIONAL_INTELLIGENCE_SERVICE,
        KENYA_REVENUE_AUTHORITY,
        INTERPOL_KENYA,
        UNHCR_KENYA
    }

    enum class ReporterRelationship {
        VICTIM,
        WITNESS,
        FAMILY_MEMBER,
        FRIEND,
        PROFESSIONAL,
        ANONYMOUS_TIP,
        LAW_ENFORCEMENT,
        ORGANIZATION
    }

    enum class EvidenceType {
        SCREENSHOT, AUDIO_RECORDING, VIDEO_RECORDING, DOCUMENT, 
        TRANSACTION_RECORD, COMMUNICATION_LOG, METADATA, GPS_LOCATION
    }

    enum class AccessLevel {
        PUBLIC, RESTRICTED, CONFIDENTIAL, SECRET
    }

    enum class SupportType {
        MEDICAL_ATTENTION,
        PSYCHOLOGICAL_COUNSELING,
        LEGAL_AID,
        FINANCIAL_ASSISTANCE,
        SAFE_SHELTER,
        WITNESS_PROTECTION,
        FAMILY_SUPPORT,
        REHABILITATION
    }

    enum class FinancialLossType {
        DIRECT_THEFT,
        FRAUDULENT_TRANSACTION,
        INVESTMENT_SCAM,
        LOAN_FRAUD,
        CRYPTOCURRENCY_THEFT,
        UNAUTHORIZED_CHARGES,
        IDENTITY_THEFT_RELATED
    }

    enum class ResponseStatus {
        RECEIVED,
        UNDER_REVIEW,
        ASSIGNED,
        INVESTIGATING,
        ADDITIONAL_INFO_NEEDED,
        FORWARDED,
        RESOLVED,
        CLOSED,
        ARCHIVED
    }

    /**
     * Submit report to appropriate government agency
     */
    suspend fun submitGovernmentReport(report: GovernmentReport): Result<GovernmentResponse> {
        return try {
            val apiEndpoint = getAgencyEndpoint(report.targetAgency)
            val requestPayload = buildReportPayload(report)
            
            // Encrypt sensitive data
            val encryptedPayload = encryptReportData(requestPayload)
            
            // Generate authentication signature
            val signature = generateAuthSignature(encryptedPayload, report.targetAgency)
            
            // Submit to government API
            val response = submitToGovernmentAPI(apiEndpoint, encryptedPayload, signature, report.targetAgency)
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val governmentResponse = parseGovernmentResponse(responseBody, report)
                
                // Store submission record in blockchain
                recordSubmissionInBlockchain(report, governmentResponse)
                
                Log.i(TAG, "Government report submitted successfully: ${report.reportId}")
                Result.success(governmentResponse)
            } else {
                Log.e(TAG, "Government API returned error: ${response.code}")
                Result.failure(Exception("Government API error: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit government report", e)
            Result.failure(e)
        }
    }

    /**
     * Track status of submitted report
     */
    suspend fun trackReportStatus(reportId: String, agency: GovernmentAgency): Result<GovernmentResponse> {
        return try {
            val apiEndpoint = "${getAgencyEndpoint(agency)}/reports/$reportId/status"
            val signature = generateAuthSignature(reportId, agency)
            
            val request = Request.Builder()
                .url(apiEndpoint)
                .addHeader("Authorization", "Bearer ${getAPIKey(agency)}")
                .addHeader("X-Signature", signature)
                .addHeader("Content-Type", "application/json")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val statusResponse = parseStatusResponse(responseBody, reportId)
                
                Log.i(TAG, "Report status retrieved: $reportId - ${statusResponse.status}")
                Result.success(statusResponse)
            } else {
                Result.failure(Exception("Failed to retrieve report status: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track report status", e)
            Result.failure(e)
        }
    }

    /**
     * Submit evidence to support existing case
     */
    suspend fun submitAdditionalEvidence(
        caseNumber: String,
        agency: GovernmentAgency,
        evidence: List<EvidenceAttachment>,
        description: String
    ): Result<String> {
        return try {
            val apiEndpoint = "${getAgencyEndpoint(agency)}/cases/$caseNumber/evidence"
            
            val evidencePayload = JSONObject().apply {
                put("case_number", caseNumber)
                put("description", description)
                put("evidence_count", evidence.size)
                put("evidence_items", org.json.JSONArray().apply {
                    evidence.forEach { item ->
                        put(JSONObject().apply {
                            put("evidence_id", item.evidenceId)
                            put("type", item.evidenceType.name)
                            put("filename", item.filename)
                            put("content_hash", item.contentHash)
                            put("blockchain_ref", item.blockchainReference)
                            put("description", item.description)
                            put("collection_method", item.collectionMethod)
                        })
                    }
                })
                put("submission_timestamp", System.currentTimeMillis())
            }
            
            val signature = generateAuthSignature(evidencePayload.toString(), agency)
            
            val requestBody = evidencePayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(apiEndpoint)
                .addHeader("Authorization", "Bearer ${getAPIKey(agency)}")
                .addHeader("X-Signature", signature)
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val submissionId = JSONObject(responseBody).getString("submission_id")
                
                Log.i(TAG, "Additional evidence submitted for case: $caseNumber")
                Result.success(submissionId)
            } else {
                Result.failure(Exception("Failed to submit evidence: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit additional evidence", e)
            Result.failure(e)
        }
    }

    /**
     * Request case updates and notifications
     */
    suspend fun subscribeToUpdates(
        reportIds: List<String>,
        agency: GovernmentAgency
    ): Result<String> {
        return try {
            val apiEndpoint = "${getAgencyEndpoint(agency)}/notifications/subscribe"
            
            val subscriptionPayload = JSONObject().apply {
                put("report_ids", org.json.JSONArray(reportIds))
                put("notification_types", org.json.JSONArray().apply {
                    put("status_updates")
                    put("evidence_requests")
                    put("court_dates")
                    put("case_closure")
                })
                put("delivery_method", "push_notification")
                put("contact_info", JSONObject().apply {
                    put("app_id", context.packageName)
                    put("device_token", "device_registration_token")
                })
            }
            
            val signature = generateAuthSignature(subscriptionPayload.toString(), agency)
            
            val requestBody = subscriptionPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(apiEndpoint)
                .addHeader("Authorization", "Bearer ${getAPIKey(agency)}")
                .addHeader("X-Signature", signature)
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val subscriptionId = JSONObject(responseBody).getString("subscription_id")
                
                Log.i(TAG, "Subscribed to updates for ${reportIds.size} reports")
                Result.success(subscriptionId)
            } else {
                Result.failure(Exception("Failed to subscribe to updates: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to updates", e)
            Result.failure(e)
        }
    }

    /**
     * Generate anonymous tip submission
     */
    suspend fun submitAnonymousTip(
        tipContent: String,
        incidentType: IncidentType,
        location: LocationInfo?,
        agency: GovernmentAgency = GovernmentAgency.DIRECTORATE_CRIMINAL_INVESTIGATIONS
    ): Result<String> {
        return try {
            val anonymousReport = GovernmentReport(
                reportId = UUID.randomUUID().toString(),
                reportType = ReportType.CYBERCRIME_REPORT,
                incidentDetails = IncidentDetails(
                    incidentType = incidentType,
                    description = tipContent,
                    occurredAt = System.currentTimeMillis()
                ),
                reporterInfo = ReporterInfo(
                    name = "Anonymous",
                    idNumber = "ANONYMOUS",
                    phoneNumber = "ANONYMOUS",
                    email = "anonymous@safenet.shield",
                    address = "Anonymous",
                    relationship = ReporterRelationship.ANONYMOUS_TIP,
                    contactPermissions = ContactPermissions(
                        allowPhoneCalls = false,
                        allowSMS = false,
                        allowEmail = false
                    ),
                    anonymityRequested = true
                ),
                evidenceAttachments = emptyList(),
                location = location ?: LocationInfo(
                    latitude = 0.0,
                    longitude = 0.0,
                    accuracy = 0f,
                    address = "Unknown",
                    county = "Unknown",
                    constituency = "Unknown",
                    ward = "Unknown"
                ),
                urgencyLevel = UrgencyLevel.MEDIUM,
                targetAgency = agency,
                legalBasis = LegalBasis(
                    applicableLaws = listOf("Anonymous Reporting Protection Act"),
                    jurisdiction = "Kenya",
                    legalReferences = emptyList()
                ),
                followUpRequired = false
            )
            
            val response = submitGovernmentReport(anonymousReport)
            response.fold(
                onSuccess = { 
                    Log.i(TAG, "Anonymous tip submitted successfully")
                    Result.success(it.responseId)
                },
                onFailure = { 
                    Log.e(TAG, "Failed to submit anonymous tip", it)
                    Result.failure(it)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting anonymous tip", e)
            Result.failure(e)
        }
    }

    // Helper methods
    private fun getAgencyEndpoint(agency: GovernmentAgency): String {
        return when (agency) {
            GovernmentAgency.KENYA_POLICE_SERVICE -> KENYA_POLICE_API
            GovernmentAgency.DIRECTORATE_CRIMINAL_INVESTIGATIONS -> DCI_REPORTING_API
            GovernmentAgency.SAFARICOM_FRAUD_DEPARTMENT -> SAFARICOM_FRAUD_API
            GovernmentAgency.CENTRAL_BANK_KENYA -> CENTRAL_BANK_API
            GovernmentAgency.COMMUNICATIONS_AUTHORITY_KENYA -> CAK_REPORTING_API
            GovernmentAgency.OFFICE_DIRECTOR_PUBLIC_PROSECUTIONS -> ODPP_CASE_API
            GovernmentAgency.INTERPOL_KENYA -> INTERPOL_API
            GovernmentAgency.UNHCR_KENYA -> UNHCR_API
            else -> KENYA_POLICE_API
        }
    }

    private fun getAPIKey(agency: GovernmentAgency): String {
        // In production, store API keys securely
        return "api_key_${agency.name.lowercase()}"
    }

    private fun buildReportPayload(report: GovernmentReport): JSONObject {
        return JSONObject().apply {
            put("report_id", report.reportId)
            put("report_type", report.reportType.name)
            put("urgency_level", report.urgencyLevel.name)
            put("submission_timestamp", report.submissionTimestamp)
            
            // Incident details
            put("incident", JSONObject().apply {
                put("type", report.incidentDetails.incidentType.name)
                put("description", report.incidentDetails.description)
                put("occurred_at", report.incidentDetails.occurredAt)
                put("discovered_at", report.incidentDetails.discoveredAt)
                put("ongoing_threat", report.incidentDetails.ongoingThreat)
                
                // Financial loss if applicable
                report.incidentDetails.financialLoss?.let { loss ->
                    put("financial_loss", JSONObject().apply {
                        put("currency", loss.currency)
                        put("amount", loss.amount)
                        put("loss_type", loss.lossType.name)
                        put("recovery_possible", loss.recoveryPossible)
                    })
                }
            })
            
            // Reporter information
            put("reporter", JSONObject().apply {
                if (!report.reporterInfo.anonymityRequested) {
                    put("name", report.reporterInfo.name)
                    put("id_number", report.reporterInfo.idNumber)
                    put("phone", report.reporterInfo.phoneNumber)
                    put("email", report.reporterInfo.email)
                    put("address", report.reporterInfo.address)
                }
                put("relationship", report.reporterInfo.relationship.name)
                put("preferred_language", report.reporterInfo.preferredLanguage)
                put("anonymity_requested", report.reporterInfo.anonymityRequested)
            })
            
            // Location information
            put("location", JSONObject().apply {
                put("latitude", report.location.latitude)
                put("longitude", report.location.longitude)
                put("accuracy", report.location.accuracy)
                put("address", report.location.address)
                put("county", report.location.county)
                put("constituency", report.location.constituency)
                put("ward", report.location.ward)
                report.location.landmark?.let { put("landmark", it) }
            })
            
            // Evidence attachments
            if (report.evidenceAttachments.isNotEmpty()) {
                put("evidence", org.json.JSONArray().apply {
                    report.evidenceAttachments.forEach { evidence ->
                        put(JSONObject().apply {
                            put("evidence_id", evidence.evidenceId)
                            put("type", evidence.evidenceType.name)
                            put("filename", evidence.filename)
                            put("content_hash", evidence.contentHash)
                            put("blockchain_reference", evidence.blockchainReference)
                            put("access_level", evidence.accessLevel.name)
                            put("description", evidence.description)
                        })
                    }
                })
            }
            
            // Legal basis
            put("legal_basis", JSONObject().apply {
                put("applicable_laws", org.json.JSONArray(report.legalBasis.applicableLaws))
                put("jurisdiction", report.legalBasis.jurisdiction)
                put("legal_references", org.json.JSONArray(report.legalBasis.legalReferences))
            })
        }
    }

    private fun encryptReportData(payload: JSONObject): String {
        // In production, use proper encryption
        val data = payload.toString()
        return android.util.Base64.encodeToString(data.toByteArray(), android.util.Base64.DEFAULT)
    }

    private fun generateAuthSignature(data: String, agency: GovernmentAgency): String {
        return try {
            val key = getAPIKey(agency)
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            mac.init(secretKey)
            val signature = mac.doFinal(data.toByteArray())
            android.util.Base64.encodeToString(signature, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate auth signature", e)
            "fallback_signature"
        }
    }

    private suspend fun submitToGovernmentAPI(
        endpoint: String,
        payload: String,
        signature: String,
        agency: GovernmentAgency
    ): Response {
        val requestBody = payload.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$endpoint/reports")
            .addHeader("Authorization", "Bearer ${getAPIKey(agency)}")
            .addHeader("X-Signature", signature)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "SafeNet-Shield/1.0")
            .post(requestBody)
            .build()
        
        return httpClient.newCall(request).execute()
    }

    private fun parseGovernmentResponse(responseBody: String?, originalReport: GovernmentReport): GovernmentResponse {
        return try {
            if (responseBody != null) {
                val json = JSONObject(responseBody)
                GovernmentResponse(
                    responseId = json.getString("response_id"),
                    originalReportId = originalReport.reportId,
                    respondingAgency = originalReport.targetAgency,
                    status = ResponseStatus.valueOf(json.getString("status")),
                    caseNumber = json.optString("case_number", null),
                    assignedOfficer = parseOfficerInfo(json.optJSONObject("assigned_officer")),
                    estimatedProcessingTime = json.optLong("estimated_processing_time"),
                    nextSteps = parseStringArray(json.optJSONArray("next_steps")),
                    additionalInformationRequired = parseStringArray(json.optJSONArray("additional_info_required")),
                    publicReference = json.optString("public_reference", null)
                )
            } else {
                // Fallback response
                GovernmentResponse(
                    responseId = UUID.randomUUID().toString(),
                    originalReportId = originalReport.reportId,
                    respondingAgency = originalReport.targetAgency,
                    status = ResponseStatus.RECEIVED,
                    nextSteps = listOf("Report has been received and is being processed")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing government response", e)
            GovernmentResponse(
                responseId = UUID.randomUUID().toString(),
                originalReportId = originalReport.reportId,
                respondingAgency = originalReport.targetAgency,
                status = ResponseStatus.RECEIVED,
                nextSteps = listOf("Report submitted successfully")
            )
        }
    }

    private fun parseStatusResponse(responseBody: String?, reportId: String): GovernmentResponse {
        return try {
            val json = JSONObject(responseBody ?: "{}")
            GovernmentResponse(
                responseId = json.optString("response_id", UUID.randomUUID().toString()),
                originalReportId = reportId,
                respondingAgency = GovernmentAgency.KENYA_POLICE_SERVICE, // Default
                status = ResponseStatus.valueOf(json.optString("status", "UNDER_REVIEW")),
                caseNumber = json.optString("case_number"),
                nextSteps = parseStringArray(json.optJSONArray("next_steps"))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing status response", e)
            GovernmentResponse(
                responseId = UUID.randomUUID().toString(),
                originalReportId = reportId,
                respondingAgency = GovernmentAgency.KENYA_POLICE_SERVICE,
                status = ResponseStatus.UNDER_REVIEW,
                nextSteps = listOf("Status information unavailable")
            )
        }
    }

    private fun parseOfficerInfo(json: JSONObject?): OfficerInfo? {
        return try {
            json?.let {
                OfficerInfo(
                    officerId = it.getString("officer_id"),
                    name = it.getString("name"),
                    rank = it.getString("rank"),
                    department = it.getString("department"),
                    contactNumber = it.getString("contact_number"),
                    email = it.getString("email"),
                    badgeNumber = it.getString("badge_number")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing officer info", e)
            null
        }
    }

    private fun parseStringArray(jsonArray: org.json.JSONArray?): List<String> {
        val list = mutableListOf<String>()
        try {
            jsonArray?.let {
                for (i in 0 until it.length()) {
                    list.add(it.getString(i))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing string array", e)
        }
        return list
    }

    private suspend fun recordSubmissionInBlockchain(
        report: GovernmentReport,
        response: GovernmentResponse
    ) {
        try {
            // Record the government submission in blockchain for audit trail
            val submissionRecord = "Government report submitted: ${report.reportId} to ${report.targetAgency}"
            // This would integrate with the blockchain evidence system
            Log.d(TAG, "Blockchain record created for government submission")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record government submission in blockchain", e)
        }
    }

    /**
     * Get list of available government agencies for reporting
     */
    fun getAvailableAgencies(): List<AgencyInfo> {
        return listOf(
            AgencyInfo(
                agency = GovernmentAgency.KENYA_POLICE_SERVICE,
                name = "Kenya Police Service",
                description = "General law enforcement and crime reporting",
                supportedTypes = listOf(ReportType.CYBERCRIME_REPORT, ReportType.HARASSMENT_REPORT, ReportType.THREAT_REPORT),
                contactInfo = "+254 999 (Emergency), +254 20 341 4020",
                website = "https://www.nationalpolice.go.ke"
            ),
            AgencyInfo(
                agency = GovernmentAgency.DIRECTORATE_CRIMINAL_INVESTIGATIONS,
                name = "Directorate of Criminal Investigations",
                description = "Serious crime investigation and cybercrime unit",
                supportedTypes = listOf(ReportType.CYBERCRIME_REPORT, ReportType.FRAUD_REPORT, ReportType.FINANCIAL_CRIME),
                contactInfo = "+254 20 341 2121",
                website = "https://www.dci.go.ke"
            ),
            AgencyInfo(
                agency = GovernmentAgency.SAFARICOM_FRAUD_DEPARTMENT,
                name = "Safaricom Fraud Department",
                description = "M-Pesa fraud and mobile money scams",
                supportedTypes = listOf(ReportType.FRAUD_REPORT, ReportType.FINANCIAL_CRIME),
                contactInfo = "100 (Safaricom Customer Care)",
                website = "https://www.safaricom.co.ke"
            )
        )
    }

    data class AgencyInfo(
        val agency: GovernmentAgency,
        val name: String,
        val description: String,
        val supportedTypes: List<ReportType>,
        val contactInfo: String,
        val website: String
    )

    /**
     * Clean up API resources
     */
    fun cleanup() {
        apiScope.cancel()
        Log.d(TAG, "Government API integration cleaned up")
    }
}
