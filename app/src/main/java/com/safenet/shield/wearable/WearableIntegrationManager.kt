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

package com.safenet.shield.wearable

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.android.gms.wearable.*.*
import com.safenet.shield.offline.OfflineEmergencyManager
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.sqrt

/**
 * Wearable Device Integration Manager
 * Provides safety features integration with smartwatches and fitness trackers
 */
class WearableIntegrationManager(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "WearableIntegration"
        private const val PANIC_BUTTON_PATH = "/safenet/panic"
        private const val SAFETY_STATUS_PATH = "/safenet/status"
        private const val LOCATION_UPDATE_PATH = "/safenet/location"
        private const val HEALTH_MONITOR_PATH = "/safenet/health"
        
        // Sensor thresholds
        private const val FALL_DETECTION_THRESHOLD = 15.0f // m/s²
        private const val PANIC_SHAKE_THRESHOLD = 20.0f // m/s²
        private const val HEART_RATE_PANIC_THRESHOLD = 150 // BPM
        private const val HEART_RATE_EMERGENCY_THRESHOLD = 180 // BPM
    }

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }
    
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val emergencyManager = OfflineEmergencyManager(context)
    
    private var accelerometer: Sensor? = null
    private var heartRateSensor: Sensor? = null
    private var isMonitoringActive = false
    
    private var lastAccelerometerReading = FloatArray(3)
    private var lastHeartRate = 0f
    private var fallDetectionEnabled = true
    private var panicShakeEnabled = true
    private var heartRateMonitoringEnabled = true
    
    private val wearableScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    data class WearableDevice(
        val nodeId: String,
        val displayName: String,
        val deviceType: DeviceType,
        val capabilities: List<WearableCapability>,
        val batteryLevel: Int?,
        val isConnected: Boolean,
        val lastSync: Long
    )

    data class WearableSafetyState(
        val isEmergencyActive: Boolean,
        val safetyLevel: SafetyLevel,
        val locationTracking: Boolean,
        val healthMonitoring: Boolean,
        val panicButtonEnabled: Boolean,
        val lastUpdate: Long = System.currentTimeMillis()
    )

    data class HealthMetrics(
        val heartRate: Int?,
        val stressLevel: StressLevel?,
        val activityLevel: ActivityLevel,
        val sleepQuality: SleepQuality?,
        val emergencyIndicators: List<HealthAlert>,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class HealthAlert(
        val type: AlertType,
        val severity: AlertSeverity,
        val description: String,
        val recommendedAction: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    enum class DeviceType {
        SMARTWATCH, FITNESS_TRACKER, SMART_RING, HEALTH_MONITOR, UNKNOWN
    }

    enum class WearableCapability {
        PANIC_BUTTON,
        FALL_DETECTION,
        HEART_RATE_MONITORING,
        GPS_TRACKING,
        EMERGENCY_CALLING,
        MESSAGE_DISPLAY,
        VIBRATION_ALERTS,
        VOICE_COMMANDS,
        GESTURE_CONTROL
    }

    enum class SafetyLevel {
        SECURE, CAUTION, WARNING, EMERGENCY, CRITICAL
    }

    enum class StressLevel {
        LOW, MODERATE, HIGH, EXTREME
    }

    enum class ActivityLevel {
        SEDENTARY, LIGHT, MODERATE, VIGOROUS, EXTREME
    }

    enum class SleepQuality {
        EXCELLENT, GOOD, FAIR, POOR, VERY_POOR
    }

    enum class AlertType {
        HEART_RATE_ANOMALY,
        FALL_DETECTED,
        PANIC_GESTURE,
        INACTIVITY_ALERT,
        STRESS_SPIKE,
        SLEEP_DISTURBANCE
    }

    enum class AlertSeverity {
        INFO, LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Initialize wearable integration
     */
    suspend fun initializeWearableIntegration(): Result<List<WearableDevice>> {
        return try {
            // Initialize sensors
            initializeSensors()
            
            // Discover connected wearable devices
            val devices = discoverWearableDevices()
            
            // Set up data synchronization
            setupDataSync()
            
            // Start monitoring
            startSafetyMonitoring()
            
            Log.i(TAG, "Wearable integration initialized with ${devices.size} devices")
            Result.success(devices)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize wearable integration", e)
            Result.failure(e)
        }
    }

    /**
     * Discover and connect to available wearable devices
     */
    private suspend fun discoverWearableDevices(): List<WearableDevice> {
        return try {
            val connectedNodes = nodeClient.connectedNodes.await()
            val devices = mutableListOf<WearableDevice>()
            
            for (node in connectedNodes) {
                val capabilities = getNodeCapabilities(node.id)
                val device = WearableDevice(
                    nodeId = node.id,
                    displayName = node.displayName,
                    deviceType = determineDeviceType(node, capabilities),
                    capabilities = capabilities,
                    batteryLevel = null, // Would need specific API
                    isConnected = true,
                    lastSync = System.currentTimeMillis()
                )
                devices.add(device)
                
                // Send initial safety state
                sendSafetyStateToDevice(device)
            }
            
            devices
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to discover wearable devices", e)
            emptyList()
        }
    }

    /**
     * Get capabilities of a specific node
     */
    private suspend fun getNodeCapabilities(nodeId: String): List<WearableCapability> {
        return try {
            val capabilities = mutableListOf<WearableCapability>()
            
            // Check for specific capabilities
            val capabilityInfo = capabilityClient.getCapability("safenet_panic_button", CapabilityClient.FILTER_REACHABLE).await()
            if (capabilityInfo.nodes.any { it.id == nodeId }) {
                capabilities.add(WearableCapability.PANIC_BUTTON)
            }
            
            // Add other capability checks here
            capabilities.addAll(listOf(
                WearableCapability.VIBRATION_ALERTS,
                WearableCapability.MESSAGE_DISPLAY,
                WearableCapability.HEART_RATE_MONITORING
            ))
            
            capabilities
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get node capabilities", e)
            emptyList()
        }
    }

    /**
     * Determine device type based on node information
     */
    private fun determineDeviceType(node: Node, capabilities: List<WearableCapability>): DeviceType {
        return when {
            node.displayName.contains("watch", ignoreCase = true) -> DeviceType.SMARTWATCH
            node.displayName.contains("fit", ignoreCase = true) -> DeviceType.FITNESS_TRACKER
            node.displayName.contains("ring", ignoreCase = true) -> DeviceType.SMART_RING
            capabilities.contains(WearableCapability.HEART_RATE_MONITORING) -> DeviceType.HEALTH_MONITOR
            else -> DeviceType.UNKNOWN
        }
    }

    /**
     * Send safety state to wearable device
     */
    suspend fun sendSafetyStateToDevice(device: WearableDevice): Result<Boolean> {
        return try {
            val safetyState = WearableSafetyState(
                isEmergencyActive = false,
                safetyLevel = SafetyLevel.SECURE,
                locationTracking = true,
                healthMonitoring = heartRateMonitoringEnabled,
                panicButtonEnabled = true
            )
            
            val dataMap = PutDataMapRequest.create(SAFETY_STATUS_PATH).apply {
                dataMap.putBoolean("emergency_active", safetyState.isEmergencyActive)
                dataMap.putString("safety_level", safetyState.safetyLevel.name)
                dataMap.putBoolean("location_tracking", safetyState.locationTracking)
                dataMap.putBoolean("health_monitoring", safetyState.healthMonitoring)
                dataMap.putBoolean("panic_button_enabled", safetyState.panicButtonEnabled)
                dataMap.putLong("timestamp", safetyState.lastUpdate)
            }
            
            val putDataRequest = dataMap.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataRequest).await()
            
            Log.d(TAG, "Safety state sent to device: ${device.displayName}")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send safety state to device", e)
            Result.failure(e)
        }
    }

    /**
     * Handle panic button activation from wearable
     */
    suspend fun handleWearablePanicButton(nodeId: String): Result<Boolean> {
        return try {
            Log.w(TAG, "Panic button activated from wearable: $nodeId")
            
            // Trigger emergency alert
            emergencyManager.triggerEmergencyAlert(
                emergencyType = OfflineEmergencyManager.EmergencyType.GENERAL,
                customMessage = "Panic button activated from wearable device"
            )
            
            // Send confirmation back to wearable
            sendPanicConfirmationToWearable(nodeId)
            
            // Update safety state on all devices
            updateSafetyStateOnAllDevices(SafetyLevel.EMERGENCY)
            
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle wearable panic button", e)
            Result.failure(e)
        }
    }

    /**
     * Send panic confirmation to wearable device
     */
    private suspend fun sendPanicConfirmationToWearable(nodeId: String) {
        try {
            val confirmationMessage = "Panic alert activated. Emergency contacts notified."
            messageClient.sendMessage(nodeId, PANIC_BUTTON_PATH, confirmationMessage.toByteArray()).await()
            
            Log.d(TAG, "Panic confirmation sent to wearable")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send panic confirmation", e)
        }
    }

    /**
     * Update safety state on all connected devices
     */
    private suspend fun updateSafetyStateOnAllDevices(safetyLevel: SafetyLevel) {
        try {
            val connectedNodes = nodeClient.connectedNodes.await()
            
            for (node in connectedNodes) {
                val dataMap = PutDataMapRequest.create(SAFETY_STATUS_PATH).apply {
                    dataMap.putBoolean("emergency_active", safetyLevel == SafetyLevel.EMERGENCY)
                    dataMap.putString("safety_level", safetyLevel.name)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                
                val putDataRequest = dataMap.asPutDataRequest().setUrgent()
                dataClient.putDataItem(putDataRequest).await()
            }
            
            Log.d(TAG, "Safety state updated on all devices: $safetyLevel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update safety state on devices", e)
        }
    }

    /**
     * Monitor health metrics from wearable devices
     */
    suspend fun monitorHealthMetrics(): Result<HealthMetrics> {
        return try {
            val healthAlerts = mutableListOf<HealthAlert>()
            
            // Check heart rate
            if (lastHeartRate > 0) {
                when {
                    lastHeartRate > HEART_RATE_EMERGENCY_THRESHOLD -> {
                        healthAlerts.add(
                            HealthAlert(
                                type = AlertType.HEART_RATE_ANOMALY,
                                severity = AlertSeverity.CRITICAL,
                                description = "Extremely high heart rate detected: ${lastHeartRate.toInt()} BPM",
                                recommendedAction = "Seek immediate medical attention"
                            )
                        )
                    }
                    lastHeartRate > HEART_RATE_PANIC_THRESHOLD -> {
                        healthAlerts.add(
                            HealthAlert(
                                type = AlertType.HEART_RATE_ANOMALY,
                                severity = AlertSeverity.HIGH,
                                description = "High heart rate detected: ${lastHeartRate.toInt()} BPM",
                                recommendedAction = "Check if in emergency situation"
                            )
                        )
                    }
                }
            }
            
            val healthMetrics = HealthMetrics(
                heartRate = if (lastHeartRate > 0) lastHeartRate.toInt() else null,
                stressLevel = calculateStressLevel(lastHeartRate),
                activityLevel = calculateActivityLevel(),
                sleepQuality = null, // Would need sleep tracking data
                emergencyIndicators = healthAlerts
            )
            
            // Send health data to wearables if there are alerts
            if (healthAlerts.isNotEmpty()) {
                sendHealthAlertsToWearables(healthAlerts)
            }
            
            Result.success(healthMetrics)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to monitor health metrics", e)
            Result.failure(e)
        }
    }

    /**
     * Send health alerts to wearable devices
     */
    private suspend fun sendHealthAlertsToWearables(alerts: List<HealthAlert>) {
        try {
            val connectedNodes = nodeClient.connectedNodes.await()
            
            for (node in connectedNodes) {
                val alertMessage = alerts.firstOrNull { it.severity == AlertSeverity.CRITICAL }
                    ?: alerts.firstOrNull { it.severity == AlertSeverity.HIGH }
                    ?: alerts.first()
                
                messageClient.sendMessage(
                    node.id,
                    HEALTH_MONITOR_PATH,
                    alertMessage.description.toByteArray()
                ).await()
            }
            
            Log.d(TAG, "Health alerts sent to wearables")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send health alerts to wearables", e)
        }
    }

    // Sensor monitoring implementation
    private fun initializeSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    private fun startSafetyMonitoring() {
        if (!isMonitoringActive) {
            accelerometer?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            
            heartRateSensor?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            
            isMonitoringActive = true
            Log.d(TAG, "Safety monitoring started")
        }
    }

    fun stopSafetyMonitoring() {
        if (isMonitoringActive) {
            sensorManager.unregisterListener(this)
            isMonitoringActive = false
            Log.d(TAG, "Safety monitoring stopped")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(sensorEvent.values)
                Sensor.TYPE_HEART_RATE -> handleHeartRateData(sensorEvent.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun handleAccelerometerData(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        
        // Calculate acceleration magnitude
        val acceleration = sqrt(x * x + y * y + z * z)
        
        // Fall detection
        if (fallDetectionEnabled && acceleration > FALL_DETECTION_THRESHOLD) {
            wearableScope.launch {
                handleFallDetection(acceleration)
            }
        }
        
        // Panic shake detection (rapid movement)
        if (panicShakeEnabled && acceleration > PANIC_SHAKE_THRESHOLD) {
            wearableScope.launch {
                handlePanicShake(acceleration)
            }
        }
        
        lastAccelerometerReading = values.clone()
    }

    private fun handleHeartRateData(heartRate: Float) {
        lastHeartRate = heartRate
        
        if (heartRateMonitoringEnabled && heartRate > HEART_RATE_PANIC_THRESHOLD) {
            wearableScope.launch {
                handleHeartRateAlert(heartRate)
            }
        }
    }

    private suspend fun handleFallDetection(acceleration: Float) {
        try {
            Log.w(TAG, "Fall detected with acceleration: $acceleration")
            
            // Create health alert
            val fallAlert = HealthAlert(
                type = AlertType.FALL_DETECTED,
                severity = AlertSeverity.HIGH,
                description = "Fall detected with high impact",
                recommendedAction = "Check user status and call for help if needed"
            )
            
            // Notify wearables
            sendHealthAlertsToWearables(listOf(fallAlert))
            
            // Trigger emergency if acceleration is extreme
            if (acceleration > FALL_DETECTION_THRESHOLD * 1.5) {
                emergencyManager.triggerEmergencyAlert(
                    emergencyType = OfflineEmergencyManager.EmergencyType.MEDICAL,
                    customMessage = "Severe fall detected - possible injury"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle fall detection", e)
        }
    }

    private suspend fun handlePanicShake(acceleration: Float) {
        try {
            Log.w(TAG, "Panic shake detected with acceleration: $acceleration")
            
            // Trigger emergency alert
            emergencyManager.triggerEmergencyAlert(
                emergencyType = OfflineEmergencyManager.EmergencyType.GENERAL,
                customMessage = "Panic gesture detected - shake pattern"
            )
            
            // Update wearables
            updateSafetyStateOnAllDevices(SafetyLevel.EMERGENCY)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle panic shake", e)
        }
    }

    private suspend fun handleHeartRateAlert(heartRate: Float) {
        try {
            Log.w(TAG, "Heart rate alert: $heartRate BPM")
            
            val severity = when {
                heartRate > HEART_RATE_EMERGENCY_THRESHOLD -> AlertSeverity.CRITICAL
                heartRate > HEART_RATE_PANIC_THRESHOLD -> AlertSeverity.HIGH
                else -> AlertSeverity.MEDIUM
            }
            
            val alert = HealthAlert(
                type = AlertType.HEART_RATE_ANOMALY,
                severity = severity,
                description = "Elevated heart rate: ${heartRate.toInt()} BPM",
                recommendedAction = if (severity == AlertSeverity.CRITICAL) "Seek immediate medical attention" else "Monitor situation"
            )
            
            sendHealthAlertsToWearables(listOf(alert))
            
            if (severity == AlertSeverity.CRITICAL) {
                emergencyManager.triggerEmergencyAlert(
                    emergencyType = OfflineEmergencyManager.EmergencyType.MEDICAL,
                    customMessage = "Critical heart rate detected: ${heartRate.toInt()} BPM"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle heart rate alert", e)
        }
    }

    // Helper methods
    private fun calculateStressLevel(heartRate: Float): StressLevel {
        return when {
            heartRate <= 0 -> StressLevel.LOW
            heartRate < 80 -> StressLevel.LOW
            heartRate < 100 -> StressLevel.MODERATE
            heartRate < 120 -> StressLevel.HIGH
            else -> StressLevel.EXTREME
        }
    }

    private fun calculateActivityLevel(): ActivityLevel {
        val acceleration = sqrt(
            lastAccelerometerReading[0] * lastAccelerometerReading[0] +
            lastAccelerometerReading[1] * lastAccelerometerReading[1] +
            lastAccelerometerReading[2] * lastAccelerometerReading[2]
        )
        
        return when {
            acceleration < 2 -> ActivityLevel.SEDENTARY
            acceleration < 5 -> ActivityLevel.LIGHT
            acceleration < 10 -> ActivityLevel.MODERATE
            acceleration < 15 -> ActivityLevel.VIGOROUS
            else -> ActivityLevel.EXTREME
        }
    }

    private suspend fun setupDataSync() {
        // Set up automatic data synchronization with wearables
        Log.d(TAG, "Setting up wearable data synchronization")
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopSafetyMonitoring()
        wearableScope.cancel()
    }
}
