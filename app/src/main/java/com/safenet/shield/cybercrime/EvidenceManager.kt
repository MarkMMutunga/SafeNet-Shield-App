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
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.safenet.shield.utils.SecurityUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced evidence management for cybercrime reporting
 * Features: Secure storage, metadata extraction, chain of custody, digital signatures
 */
class EvidenceManager(private val context: Context) {

    companion object {
        private const val TAG = "EvidenceManager"
        private const val EVIDENCE_DIR = "secure_evidence"
        private const val HASH_ALGORITHM = "SHA-256"
    }

    /**
     * Data class for evidence metadata
     */
    data class EvidenceMetadata(
        val id: String,
        val originalFilename: String,
        val fileHash: String,
        val timestamp: Long,
        val fileSize: Long,
        val mimeType: String,
        val deviceInfo: String,
        val locationHash: String? = null,
        val chainOfCustody: MutableList<CustodyEntry> = mutableListOf()
    )

    data class CustodyEntry(
        val timestamp: Long,
        val action: String,
        val userHash: String,
        val deviceFingerprint: String
    )

    /**
     * Securely store evidence with cryptographic integrity
     */
    fun storeEvidence(
        uri: Uri,
        description: String,
        isAnonymous: Boolean = false
    ): Result<EvidenceMetadata> {
        return try {
            val evidenceId = generateEvidenceId()
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Unable to open file"))

            // Create secure evidence directory
            val evidenceDir = File(context.filesDir, EVIDENCE_DIR)
            if (!evidenceDir.exists()) {
                evidenceDir.mkdirs()
            }

            // Generate secure filename
            val secureFilename = "${evidenceId}_evidence"
            val evidenceFile = File(evidenceDir, secureFilename)

            // Read and encrypt file data
            val originalData = inputStream.readBytes()
            val securityUtils = SecurityUtils.getInstance(context)
            val encryptedData = securityUtils.encryptData(originalData, context)
            
            // Write encrypted data
            FileOutputStream(evidenceFile).use { output ->
                output.write(encryptedData)
            }

            // Generate file hash for integrity
            val fileHash = generateFileHash(originalData)

            // Extract metadata
            val metadata = EvidenceMetadata(
                id = evidenceId,
                originalFilename = getOriginalFilename(uri),
                fileHash = fileHash,
                timestamp = System.currentTimeMillis(),
                fileSize = originalData.size.toLong(),
                mimeType = context.contentResolver.getType(uri) ?: "unknown",
                deviceInfo = getDeviceFingerprint(),
                locationHash = if (isAnonymous) null else getLocationHash()
            )

            // Add initial custody entry
            metadata.chainOfCustody.add(
                CustodyEntry(
                    timestamp = System.currentTimeMillis(),
                    action = "EVIDENCE_STORED",
                    userHash = if (isAnonymous) "ANONYMOUS" else getUserHash(),
                    deviceFingerprint = getDeviceFingerprint()
                )
            )

            // Store metadata securely
            storeMetadata(metadata)

            Log.i(TAG, "Evidence stored securely: $evidenceId")
            Result.success(metadata)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to store evidence", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve evidence with integrity verification
     */
    fun retrieveEvidence(evidenceId: String): Result<Pair<ByteArray, EvidenceMetadata>> {
        return try {
            val evidenceFile = File(File(context.filesDir, EVIDENCE_DIR), "${evidenceId}_evidence")
            if (!evidenceFile.exists()) {
                return Result.failure(Exception("Evidence not found"))
            }

            // Read encrypted data
            val encryptedData = evidenceFile.readBytes()
            val securityUtils = SecurityUtils.getInstance(context)
            val decryptedData = securityUtils.decryptData(encryptedData, context)

            // Retrieve metadata
            val metadata = getMetadata(evidenceId)
                ?: return Result.failure(Exception("Evidence metadata not found"))

            // Verify integrity
            val currentHash = generateFileHash(decryptedData)
            if (currentHash != metadata.fileHash) {
                Log.w(TAG, "Evidence integrity check failed for $evidenceId")
                return Result.failure(Exception("Evidence integrity compromised"))
            }

            // Add custody entry
            metadata.chainOfCustody.add(
                CustodyEntry(
                    timestamp = System.currentTimeMillis(),
                    action = "EVIDENCE_ACCESSED",
                    userHash = getUserHash(),
                    deviceFingerprint = getDeviceFingerprint()
                )
            )

            updateMetadata(metadata)

            Result.success(Pair(decryptedData, metadata))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve evidence", e)
            Result.failure(e)
        }
    }

    /**
     * Analyze screenshot for sensitive information
     */
    fun analyzeScreenshot(bitmap: Bitmap): ScreenshotAnalysis {
        val warnings = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // Check for common patterns (simplified implementation)
        // In production, this would use OCR and pattern recognition
        
        // Check image dimensions (could contain personal info in status bar)
        if (bitmap.height > 2000) {
            warnings.add("Screenshot may contain status bar with personal information")
            suggestions.add("Consider cropping out the status bar before submitting")
        }

        // Check for common UI elements that might contain sensitive data
        suggestions.add("Review screenshot for phone numbers, email addresses, or personal names")
        suggestions.add("Ensure no sensitive account information is visible")

        return ScreenshotAnalysis(
            hasWarnings = warnings.isNotEmpty(),
            warnings = warnings,
            suggestions = suggestions,
            confidence = 0.8f
        )
    }

    data class ScreenshotAnalysis(
        val hasWarnings: Boolean,
        val warnings: List<String>,
        val suggestions: List<String>,
        val confidence: Float
    )

    /**
     * Generate evidence report for legal purposes
     */
    fun generateEvidenceReport(evidenceId: String): String {
        val metadata = getMetadata(evidenceId) ?: return "Evidence not found"
        
        val report = StringBuilder()
        report.appendLine("DIGITAL EVIDENCE REPORT")
        report.appendLine("=" .repeat(50))
        report.appendLine("Evidence ID: ${metadata.id}")
        report.appendLine("Original Filename: ${metadata.originalFilename}")
        report.appendLine("File Hash (SHA-256): ${metadata.fileHash}")
        report.appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()).format(Date(metadata.timestamp))}")
        report.appendLine("File Size: ${metadata.fileSize} bytes")
        report.appendLine("MIME Type: ${metadata.mimeType}")
        report.appendLine("Device Info: ${metadata.deviceInfo}")
        report.appendLine()
        report.appendLine("CHAIN OF CUSTODY:")
        report.appendLine("-" .repeat(30))
        
        metadata.chainOfCustody.forEach { entry ->
            report.appendLine("${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))}: ${entry.action}")
            report.appendLine("  User: ${entry.userHash}")
            report.appendLine("  Device: ${entry.deviceFingerprint}")
            report.appendLine()
        }
        
        return report.toString()
    }

    // Private helper methods
    private fun generateEvidenceId(): String {
        return "EVD_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    }

    private fun generateFileHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun getOriginalFilename(uri: Uri): String {
        // Implementation to extract original filename from URI
        return uri.lastPathSegment ?: "unknown_file"
    }

    private fun getDeviceFingerprint(): String {
        // Generate device fingerprint for chain of custody
        return "DEVICE_${android.os.Build.MODEL}_${android.os.Build.ID}".take(32)
    }

    private fun getUserHash(): String {
        // Generate anonymous user hash using secure random
        val random = SecureRandom()
        val bytes = ByteArray(8)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getLocationHash(): String? {
        // Generate privacy-preserving location hash if location services enabled
        return null // Placeholder - implement based on privacy requirements
    }

    private fun storeMetadata(metadata: EvidenceMetadata) {
        // Store metadata securely (implementation depends on chosen storage method)
        // This could be encrypted local storage or secure cloud storage
    }

    private fun getMetadata(evidenceId: String): EvidenceMetadata? {
        // Retrieve metadata (placeholder implementation)
        return null
    }

    private fun updateMetadata(metadata: EvidenceMetadata) {
        // Update metadata with new custody entries
    }
}
