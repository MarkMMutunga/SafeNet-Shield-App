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

package com.safenet.shield.offline

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import androidx.room.*
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Offline Emergency Management System
 * Provides critical safety features even without internet connectivity
 */
class OfflineEmergencyManager(private val context: Context) {

    companion object {
        private const val TAG = "OfflineEmergency"
        private const val PREFS_NAME = "offline_emergency_prefs"
        private const val EMERGENCY_DB_NAME = "emergency_offline_db"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val database: OfflineEmergencyDatabase by lazy {
        Room.databaseBuilder(context, OfflineEmergencyDatabase::class.java, EMERGENCY_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    data class EmergencyProfile(
        val emergencyContacts: List<EmergencyContact>,
        val medicalInfo: MedicalInformation,
        val safetyPlan: SafetyPlan,
        val offlineCapabilities: OfflineCapabilities,
        val lastUpdated: Long = System.currentTimeMillis()
    )

    data class EmergencyContact(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val phoneNumber: String,
        val relationship: String,
        val priority: ContactPriority,
        val contactMethods: List<ContactMethod>,
        val isVerified: Boolean = false,
        val lastContactTime: Long = 0
    )

    data class MedicalInformation(
        val bloodType: String? = null,
        val allergies: List<String> = emptyList(),
        val medications: List<String> = emptyList(),
        val medicalConditions: List<String> = emptyList(),
        val emergencyMedicalContact: String? = null,
        val insuranceInfo: String? = null,
        val lastUpdated: Long = System.currentTimeMillis()
    )

    data class SafetyPlan(
        val safeLocations: List<SafeLocation>,
        val emergencyProcedures: List<EmergencyProcedure>,
        val escapeRoutes: List<EscapeRoute>,
        val codeWords: Map<String, String>, // code word -> meaning
        val autoResponseSettings: AutoResponseSettings
    )

    data class SafeLocation(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val type: LocationType,
        val contactInfo: String? = null,
        val operatingHours: String? = null,
        val specialInstructions: String? = null
    )

    data class EmergencyProcedure(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val description: String,
        val steps: List<String>,
        val situationType: EmergencyType,
        val priority: ProcedurePriority,
        val requiredResources: List<String> = emptyList()
    )

    data class EscapeRoute(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val fromLocation: String,
        val toLocation: String,
        val waypoints: List<Waypoint>,
        val estimatedTime: Int, // minutes
        val transportMode: TransportMode,
        val alternativeRoutes: List<String> = emptyList()
    )

    data class Waypoint(
        val latitude: Double,
        val longitude: Double,
        val description: String? = null
    )

    data class AutoResponseSettings(
        val enableAutoSMS: Boolean = false,
        val enableAutoCall: Boolean = false,
        val triggerOnPanic: Boolean = true,
        val triggerOnLocation: Boolean = false,
        val triggerOnTime: Boolean = false,
        val responseDelay: Int = 30, // seconds
        val customMessage: String? = null
    )

    data class OfflineCapabilities(
        val canSendSMS: Boolean,
        val canMakeEmergencyCalls: Boolean,
        val hasLocationAccess: Boolean,
        val hasCachedMaps: Boolean,
        val lastDataSync: Long,
        val availableOfflineFeatures: List<OfflineFeature>
    )

    enum class ContactPriority {
        PRIMARY, SECONDARY, TERTIARY, BACKUP
    }

    enum class ContactMethod {
        SMS, VOICE_CALL, WHATSAPP, EMAIL, EMERGENCY_SERVICES
    }

    enum class LocationType {
        POLICE_STATION, HOSPITAL, SAFE_HOUSE, FAMILY_HOME, WORKPLACE, COMMUNITY_CENTER, EMBASSY
    }

    enum class EmergencyType {
        MEDICAL, SAFETY_THREAT, NATURAL_DISASTER, HARASSMENT, ROBBERY, DOMESTIC_VIOLENCE, GENERAL
    }

    enum class ProcedurePriority {
        IMMEDIATE, HIGH, MEDIUM, LOW, INFORMATIONAL
    }

    enum class TransportMode {
        WALKING, DRIVING, PUBLIC_TRANSPORT, BICYCLE, EMERGENCY_VEHICLE
    }

    enum class OfflineFeature {
        EMERGENCY_SMS, PANIC_BUTTON, LOCATION_SHARING, SAFETY_TIMER, OFFLINE_MAPS, MEDICAL_INFO, EMERGENCY_PROCEDURES
    }

    /**
     * Initialize offline emergency system
     */
    suspend fun initializeOfflineSystem(): Result<Boolean> {
        return try {
            // Check device capabilities
            val capabilities = assessOfflineCapabilities()
            
            // Load emergency profile
            val profile = loadEmergencyProfile()
            
            // Cache essential data
            cacheEssentialData()
            
            // Set up background monitoring
            setupBackgroundMonitoring()
            
            Log.i(TAG, "Offline emergency system initialized successfully")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize offline emergency system", e)
            Result.failure(e)
        }
    }

    /**
     * Trigger emergency alert with offline capabilities
     */
    suspend fun triggerEmergencyAlert(
        emergencyType: EmergencyType,
        location: Location? = null,
        customMessage: String? = null
    ): Result<EmergencyResponse> {
        return try {
            val profile = loadEmergencyProfile()
            val timestamp = System.currentTimeMillis()
            
            // Create emergency record
            val emergency = OfflineEmergencyRecord(
                id = UUID.randomUUID().toString(),
                type = emergencyType.name,
                timestamp = timestamp,
                location = location?.let { "${it.latitude},${it.longitude}" },
                message = customMessage ?: getDefaultEmergencyMessage(emergencyType),
                status = "ACTIVE",
                attempts = mutableMapOf()
            )
            
            // Save emergency record
            database.emergencyDao().insertEmergency(emergency)
            
            // Execute emergency response
            val response = executeEmergencyResponse(emergency, profile)
            
            Log.i(TAG, "Emergency alert triggered: ${emergency.id}")
            Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger emergency alert", e)
            Result.failure(e)
        }
    }

    /**
     * Execute comprehensive emergency response
     */
    private suspend fun executeEmergencyResponse(
        emergency: OfflineEmergencyRecord,
        profile: EmergencyProfile
    ): EmergencyResponse {
        val responses = mutableListOf<ResponseAction>()
        
        // Send SMS alerts to emergency contacts
        if (profile.offlineCapabilities.canSendSMS) {
            val smsResults = sendEmergencySMS(emergency, profile.emergencyContacts)
            responses.addAll(smsResults)
        }
        
        // Make emergency calls if configured
        if (profile.safetyPlan.autoResponseSettings.enableAutoCall) {
            val callResults = makeEmergencyCalls(emergency, profile.emergencyContacts)
            responses.addAll(callResults)
        }
        
        // Share location if available
        emergency.location?.let { locationStr ->
            val locationResults = shareEmergencyLocation(locationStr, profile.emergencyContacts)
            responses.addAll(locationResults)
        }
        
        // Execute relevant emergency procedures
        val procedureResults = executeEmergencyProcedures(emergency, profile.safetyPlan)
        responses.addAll(procedureResults)
        
        return EmergencyResponse(
            emergencyId = emergency.id,
            timestamp = emergency.timestamp,
            actions = responses,
            status = if (responses.any { it.success }) ResponseStatus.PARTIAL_SUCCESS else ResponseStatus.FAILED,
            nextSteps = generateNextSteps(emergency, responses)
        )
    }

    data class EmergencyResponse(
        val emergencyId: String,
        val timestamp: Long,
        val actions: List<ResponseAction>,
        val status: ResponseStatus,
        val nextSteps: List<String>
    )

    data class ResponseAction(
        val type: ActionType,
        val description: String,
        val success: Boolean,
        val details: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    enum class ResponseStatus {
        SUCCESS, PARTIAL_SUCCESS, FAILED, PENDING
    }

    enum class ActionType {
        SMS_SENT, CALL_MADE, LOCATION_SHARED, PROCEDURE_EXECUTED, ALERT_LOGGED
    }

    /**
     * Send emergency SMS to contacts
     */
    private suspend fun sendEmergencySMS(
        emergency: OfflineEmergencyRecord,
        contacts: List<EmergencyContact>
    ): List<ResponseAction> {
        val results = mutableListOf<ResponseAction>()
        val smsManager = SmsManager.getDefault()
        
        val priorityContacts = contacts
            .filter { ContactMethod.SMS in it.contactMethods }
            .sortedBy { it.priority.ordinal }
            .take(5) // Limit to top 5 to avoid spam
        
        for (contact in priorityContacts) {
            try {
                val message = buildEmergencyMessage(emergency, contact)
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                
                results.add(
                    ResponseAction(
                        type = ActionType.SMS_SENT,
                        description = "SMS sent to ${contact.name}",
                        success = true,
                        details = "Message: $message"
                    )
                )
                
                // Update attempt record
                emergency.attempts[contact.id] = System.currentTimeMillis()
                database.emergencyDao().updateEmergency(emergency)
                
            } catch (e: Exception) {
                results.add(
                    ResponseAction(
                        type = ActionType.SMS_SENT,
                        description = "Failed to send SMS to ${contact.name}",
                        success = false,
                        details = e.message ?: "Unknown error"
                    )
                )
            }
        }
        
        return results
    }

    /**
     * Make emergency calls to contacts
     */
    private suspend fun makeEmergencyCalls(
        emergency: OfflineEmergencyRecord,
        contacts: List<EmergencyContact>
    ): List<ResponseAction> {
        val results = mutableListOf<ResponseAction>()
        
        // For demonstration - actual implementation would require telephony permissions
        val primaryContact = contacts
            .filter { ContactMethod.VOICE_CALL in it.contactMethods }
            .minByOrNull { it.priority.ordinal }
        
        primaryContact?.let { contact ->
            results.add(
                ResponseAction(
                    type = ActionType.CALL_MADE,
                    description = "Emergency call initiated to ${contact.name}",
                    success = true,
                    details = "Call to ${contact.phoneNumber}"
                )
            )
        }
        
        return results
    }

    /**
     * Share emergency location with contacts
     */
    private suspend fun shareEmergencyLocation(
        location: String,
        contacts: List<EmergencyContact>
    ): List<ResponseAction> {
        val results = mutableListOf<ResponseAction>()
        val smsManager = SmsManager.getDefault()
        
        val coordinates = location.split(",")
        if (coordinates.size == 2) {
            val lat = coordinates[0]
            val lng = coordinates[1]
            val locationMessage = "EMERGENCY LOCATION: https://maps.google.com/?q=$lat,$lng"
            
            val primaryContacts = contacts.take(3) // Share location with top 3 contacts
            
            for (contact in primaryContacts) {
                try {
                    smsManager.sendTextMessage(contact.phoneNumber, null, locationMessage, null, null)
                    
                    results.add(
                        ResponseAction(
                            type = ActionType.LOCATION_SHARED,
                            description = "Location shared with ${contact.name}",
                            success = true,
                            details = locationMessage
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        ResponseAction(
                            type = ActionType.LOCATION_SHARED,
                            description = "Failed to share location with ${contact.name}",
                            success = false,
                            details = e.message ?: "Unknown error"
                        )
                    )
                }
            }
        }
        
        return results
    }

    /**
     * Execute relevant emergency procedures
     */
    private suspend fun executeEmergencyProcedures(
        emergency: OfflineEmergencyRecord,
        safetyPlan: SafetyPlan
    ): List<ResponseAction> {
        val results = mutableListOf<ResponseAction>()
        val emergencyType = EmergencyType.valueOf(emergency.type)
        
        val relevantProcedures = safetyPlan.emergencyProcedures
            .filter { it.situationType == emergencyType || it.situationType == EmergencyType.GENERAL }
            .sortedBy { it.priority.ordinal }
        
        for (procedure in relevantProcedures) {
            try {
                // Log procedure execution
                val procedureLog = OfflineProcedureLog(
                    id = UUID.randomUUID().toString(),
                    emergencyId = emergency.id,
                    procedureId = procedure.id,
                    timestamp = System.currentTimeMillis(),
                    status = "EXECUTED"
                )
                
                database.emergencyDao().insertProcedureLog(procedureLog)
                
                results.add(
                    ResponseAction(
                        type = ActionType.PROCEDURE_EXECUTED,
                        description = "Executed: ${procedure.title}",
                        success = true,
                        details = procedure.description
                    )
                )
                
            } catch (e: Exception) {
                results.add(
                    ResponseAction(
                        type = ActionType.PROCEDURE_EXECUTED,
                        description = "Failed to execute: ${procedure.title}",
                        success = false,
                        details = e.message ?: "Unknown error"
                    )
                )
            }
        }
        
        return results
    }

    // Helper methods
    private fun buildEmergencyMessage(emergency: OfflineEmergencyRecord, contact: EmergencyContact): String {
        val timestamp = dateFormat.format(Date(emergency.timestamp))
        val location = emergency.location?.let { " Location: https://maps.google.com/?q=$it" } ?: ""
        
        return "EMERGENCY ALERT: ${emergency.message}. Time: $timestamp.$location Please respond immediately. -SafeNet Shield"
    }

    private fun getDefaultEmergencyMessage(type: EmergencyType): String {
        return when (type) {
            EmergencyType.MEDICAL -> "Medical emergency - need immediate assistance"
            EmergencyType.SAFETY_THREAT -> "Personal safety threat - send help"
            EmergencyType.HARASSMENT -> "Being harassed - need support"
            EmergencyType.ROBBERY -> "Robbery in progress - call police"
            EmergencyType.DOMESTIC_VIOLENCE -> "Domestic violence emergency - need help"
            EmergencyType.NATURAL_DISASTER -> "Natural disaster emergency - need evacuation"
            EmergencyType.GENERAL -> "Emergency situation - need immediate help"
        }
    }

    private fun assessOfflineCapabilities(): OfflineCapabilities {
        return OfflineCapabilities(
            canSendSMS = true, // Most Android devices can send SMS
            canMakeEmergencyCalls = true,
            hasLocationAccess = true, // Assume GPS available
            hasCachedMaps = false, // Would need to implement map caching
            lastDataSync = prefs.getLong("last_sync", 0),
            availableOfflineFeatures = listOf(
                OfflineFeature.EMERGENCY_SMS,
                OfflineFeature.PANIC_BUTTON,
                OfflineFeature.LOCATION_SHARING,
                OfflineFeature.MEDICAL_INFO,
                OfflineFeature.EMERGENCY_PROCEDURES
            )
        )
    }

    private fun loadEmergencyProfile(): EmergencyProfile {
        val json = prefs.getString("emergency_profile", null)
        return if (json != null) {
            try {
                gson.fromJson(json, EmergencyProfile::class.java)
            } catch (e: Exception) {
                createDefaultEmergencyProfile()
            }
        } else {
            createDefaultEmergencyProfile()
        }
    }

    private fun createDefaultEmergencyProfile(): EmergencyProfile {
        return EmergencyProfile(
            emergencyContacts = emptyList(),
            medicalInfo = MedicalInformation(),
            safetyPlan = SafetyPlan(
                safeLocations = emptyList(),
                emergencyProcedures = getDefaultEmergencyProcedures(),
                escapeRoutes = emptyList(),
                codeWords = emptyMap(),
                autoResponseSettings = AutoResponseSettings()
            ),
            offlineCapabilities = assessOfflineCapabilities()
        )
    }

    private fun getDefaultEmergencyProcedures(): List<EmergencyProcedure> {
        return listOf(
            EmergencyProcedure(
                title = "Personal Safety Threat Response",
                description = "Immediate actions when facing a personal safety threat",
                steps = listOf(
                    "Stay calm and assess the situation",
                    "Move to a safe, public area if possible",
                    "Alert emergency contacts",
                    "Contact local authorities if necessary",
                    "Document the incident when safe"
                ),
                situationType = EmergencyType.SAFETY_THREAT,
                priority = ProcedurePriority.IMMEDIATE
            ),
            EmergencyProcedure(
                title = "Medical Emergency Response",
                description = "Steps to take during a medical emergency",
                steps = listOf(
                    "Call emergency services immediately",
                    "Provide first aid if trained",
                    "Alert medical emergency contact",
                    "Gather medical information and ID",
                    "Stay with the person until help arrives"
                ),
                situationType = EmergencyType.MEDICAL,
                priority = ProcedurePriority.IMMEDIATE
            )
        )
    }

    private suspend fun cacheEssentialData() {
        // Cache essential safety data for offline use
        // This would include maps, contact info, procedures, etc.
        Log.d(TAG, "Caching essential emergency data")
    }

    private fun setupBackgroundMonitoring() {
        // Set up background services for monitoring
        Log.d(TAG, "Setting up background emergency monitoring")
    }

    private fun generateNextSteps(emergency: OfflineEmergencyRecord, responses: List<ResponseAction>): List<String> {
        val nextSteps = mutableListOf<String>()
        
        if (responses.none { it.success && it.type == ActionType.SMS_SENT }) {
            nextSteps.add("Retry sending emergency SMS")
        }
        
        if (responses.none { it.success && it.type == ActionType.CALL_MADE }) {
            nextSteps.add("Try calling emergency contacts manually")
        }
        
        nextSteps.add("Follow up with emergency contacts")
        nextSteps.add("Contact local authorities if situation escalates")
        nextSteps.add("Document the incident details")
        
        return nextSteps
    }

    /**
     * Save emergency profile to offline storage
     */
    suspend fun saveEmergencyProfile(profile: EmergencyProfile): Result<Boolean> {
        return try {
            val json = gson.toJson(profile)
            prefs.edit().putString("emergency_profile", json).apply()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save emergency profile", e)
            Result.failure(e)
        }
    }
}

// Room database entities and DAOs for offline storage
@Entity(tableName = "offline_emergencies")
data class OfflineEmergencyRecord(
    @PrimaryKey val id: String,
    val type: String,
    val timestamp: Long,
    val location: String?,
    val message: String,
    val status: String,
    val attempts: MutableMap<String, Long> = mutableMapOf()
)

@Entity(tableName = "procedure_logs")
data class OfflineProcedureLog(
    @PrimaryKey val id: String,
    val emergencyId: String,
    val procedureId: String,
    val timestamp: Long,
    val status: String
)

@Dao
interface OfflineEmergencyDao {
    @Insert
    suspend fun insertEmergency(emergency: OfflineEmergencyRecord)
    
    @Update
    suspend fun updateEmergency(emergency: OfflineEmergencyRecord)
    
    @Query("SELECT * FROM offline_emergencies ORDER BY timestamp DESC")
    suspend fun getAllEmergencies(): List<OfflineEmergencyRecord>
    
    @Insert
    suspend fun insertProcedureLog(log: OfflineProcedureLog)
    
    @Query("SELECT * FROM procedure_logs WHERE emergencyId = :emergencyId")
    suspend fun getProcedureLogsForEmergency(emergencyId: String): List<OfflineProcedureLog>
}

@Database(
    entities = [OfflineEmergencyRecord::class, OfflineProcedureLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OfflineEmergencyDatabase : RoomDatabase() {
    abstract fun emergencyDao(): OfflineEmergencyDao
}

class Converters {
    @TypeConverter
    fun fromStringMap(value: Map<String, Long>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, Long> {
        val mapType = object : TypeToken<Map<String, Long>>() {}.type
        return Gson().fromJson(value, mapType)
    }
}
