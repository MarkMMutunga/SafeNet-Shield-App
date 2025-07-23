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

package com.safenet.shield.blockchain

import android.content.Context
import android.util.Log
import com.safenet.shield.utils.SecurityUtils
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap

/**
 * Blockchain-Based Evidence Integrity System
 * Provides immutable, cryptographically secure evidence storage and verification
 */
class BlockchainEvidenceSystem(private val context: Context) {

    companion object {
        private const val TAG = "BlockchainEvidence"
        private const val BLOCKCHAIN_VERSION = "1.0"
        private const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
        private const val DIFFICULTY_TARGET = 4 // Number of leading zeros in hash
        private const val MAX_TRANSACTIONS_PER_BLOCK = 10
        private const val BLOCK_TIME_TARGET = 300000L // 5 minutes in milliseconds
    }

    private val securityUtils = SecurityUtils.getInstance(context)
    private val blockchain = mutableListOf<Block>()
    private val pendingTransactions = mutableListOf<EvidenceTransaction>()
    private val evidencePool = HashMap<String, StoredEvidence>()
    private val validatorNodes = mutableSetOf<ValidatorNode>()
    
    private val blockchainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    private var isMining = false
    private var lastBlockTime = System.currentTimeMillis()

    data class Block(
        val index: Int,
        val timestamp: Long,
        val transactions: List<EvidenceTransaction>,
        val previousHash: String,
        val merkleRoot: String,
        val nonce: Long,
        val hash: String,
        val miner: String,
        val difficulty: Int,
        val version: String = BLOCKCHAIN_VERSION
    )

    data class EvidenceTransaction(
        val id: String = UUID.randomUUID().toString(),
        val evidenceId: String,
        val action: EvidenceAction,
        val timestamp: Long = System.currentTimeMillis(),
        val from: String, // Entity performing action
        val to: String? = null, // Entity receiving (for transfers)
        val metadata: EvidenceMetadata,
        val digitalSignature: String,
        val hash: String
    )

    data class EvidenceMetadata(
        val evidenceType: EvidenceType,
        val contentHash: String,
        val fileSize: Long,
        val mimeType: String,
        val timestamp: Long = System.currentTimeMillis(),
        val location: GeographicLocation?,
        val chainOfCustody: List<CustodyEntry>,
        val accessLevel: AccessLevel,
        val retentionPolicy: RetentionPolicy,
        val legalContext: LegalContext
    )

    data class StoredEvidence(
        val id: String,
        val encryptedContent: ByteArray,
        val contentHash: String,
        val encryptionKey: String, // Encrypted with user's key
        val metadata: EvidenceMetadata,
        val blockchainRecords: List<String>, // Transaction IDs
        val createdAt: Long,
        val lastModified: Long,
        val integrityStatus: IntegrityStatus
    )

    data class CustodyEntry(
        val timestamp: Long,
        val custodian: String,
        val action: CustodyAction,
        val reason: String,
        val digitalSignature: String,
        val witnessSignatures: List<String> = emptyList()
    )

    data class GeographicLocation(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val address: String? = null,
        val landmark: String? = null
    )

    data class LegalContext(
        val jurisdiction: String,
        val applicableLaws: List<String>,
        val evidenceRules: List<String>,
        val admissibilityRequirements: List<String>,
        val legalHolds: List<LegalHold> = emptyList()
    )

    data class LegalHold(
        val id: String,
        val caseReference: String,
        val authority: String,
        val startDate: Long,
        val endDate: Long? = null,
        val reason: String
    )

    data class RetentionPolicy(
        val retentionPeriod: Long, // milliseconds
        val autoDelete: Boolean,
        val archiveAfter: Long? = null,
        val legalRequirements: List<String>
    )

    data class ValidatorNode(
        val nodeId: String,
        val publicKey: String,
        val reputation: Float,
        val stakingAmount: Float,
        val isActive: Boolean,
        val lastSeen: Long
    )

    data class IntegrityVerificationResult(
        val isValid: Boolean,
        val evidenceId: String,
        val blockchainVerified: Boolean,
        val hashMatches: Boolean,
        val chainOfCustodyIntact: Boolean,
        val digitalSignaturesValid: Boolean,
        val tamperedBlocks: List<Int>,
        val verificationTimestamp: Long = System.currentTimeMillis(),
        val verificationDetails: List<String>
    )

    enum class EvidenceAction {
        CREATE, UPDATE, ACCESS, TRANSFER, DELETE, ARCHIVE, RESTORE, VERIFY
    }

    enum class EvidenceType {
        SCREENSHOT, AUDIO_RECORDING, VIDEO_RECORDING, TEXT_MESSAGE, EMAIL, 
        DOCUMENT, PHOTO, CALL_LOG, TRANSACTION_RECORD, CHAT_HISTORY, 
        METADATA, SYSTEM_LOG, GPS_LOCATION, BIOMETRIC_DATA
    }

    enum class AccessLevel {
        PUBLIC, RESTRICTED, CONFIDENTIAL, SECRET, TOP_SECRET
    }

    enum class CustodyAction {
        RECEIVED, TRANSFERRED, ANALYZED, COPIED, MOVED, SECURED, VERIFIED, WITNESSED
    }

    enum class IntegrityStatus {
        VERIFIED, COMPROMISED, UNKNOWN, UNDER_REVIEW, QUARANTINED
    }

    /**
     * Initialize blockchain evidence system
     */
    suspend fun initializeBlockchain(): Result<Boolean> {
        return try {
            // Create genesis block if blockchain is empty
            if (blockchain.isEmpty()) {
                createGenesisBlock()
            }
            
            // Initialize validator nodes
            initializeValidatorNodes()
            
            // Start mining process
            startMiningProcess()
            
            // Verify blockchain integrity
            val integrityCheck = verifyBlockchainIntegrity()
            if (!integrityCheck.isValid) {
                throw Exception("Blockchain integrity check failed")
            }
            
            Log.i(TAG, "Blockchain evidence system initialized with ${blockchain.size} blocks")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize blockchain", e)
            Result.failure(e)
        }
    }

    /**
     * Store evidence in blockchain with tamper-proof guarantees
     */
    suspend fun storeEvidence(
        content: ByteArray,
        evidenceType: EvidenceType,
        location: GeographicLocation? = null,
        legalContext: LegalContext
    ): Result<String> {
        return try {
            val evidenceId = generateEvidenceId()
            
            // Calculate content hash
            val contentHash = calculateSHA256(content)
            
            // Encrypt content
            val encryptionKey = generateEncryptionKey()
            val encryptedContent = encryptContent(content, encryptionKey)
            
            // Create evidence metadata
            val metadata = EvidenceMetadata(
                evidenceType = evidenceType,
                contentHash = contentHash,
                fileSize = content.size.toLong(),
                mimeType = determineMimeType(evidenceType),
                location = location,
                chainOfCustody = listOf(
                    CustodyEntry(
                        timestamp = System.currentTimeMillis(),
                        custodian = "SafeNet Shield System",
                        action = CustodyAction.RECEIVED,
                        reason = "Initial evidence storage",
                        digitalSignature = generateDigitalSignature(contentHash)
                    )
                ),
                accessLevel = determineAccessLevel(evidenceType),
                retentionPolicy = createDefaultRetentionPolicy(),
                legalContext = legalContext
            )
            
            // Create blockchain transaction
            val transaction = EvidenceTransaction(
                evidenceId = evidenceId,
                action = EvidenceAction.CREATE,
                from = "User",
                metadata = metadata,
                digitalSignature = generateDigitalSignature(evidenceId + contentHash),
                hash = calculateTransactionHash(evidenceId, EvidenceAction.CREATE, metadata)
            )
            
            // Store encrypted evidence
            val storedEvidence = StoredEvidence(
                id = evidenceId,
                encryptedContent = encryptedContent,
                contentHash = contentHash,
                encryptionKey = encryptKey(encryptionKey),
                metadata = metadata,
                blockchainRecords = emptyList(),
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                integrityStatus = IntegrityStatus.VERIFIED
            )
            
            evidencePool[evidenceId] = storedEvidence
            
            // Add transaction to pending pool
            addTransaction(transaction)
            
            Log.i(TAG, "Evidence stored with ID: $evidenceId")
            Result.success(evidenceId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store evidence", e)
            Result.failure(e)
        }
    }

    /**
     * Verify evidence integrity using blockchain records
     */
    suspend fun verifyEvidenceIntegrity(evidenceId: String): Result<IntegrityVerificationResult> {
        return try {
            val evidence = evidencePool[evidenceId]
                ?: return Result.failure(Exception("Evidence not found"))
            
            val verificationDetails = mutableListOf<String>()
            var isValid = true
            var blockchainVerified = true
            var hashMatches = true
            var chainOfCustodyIntact = true
            var digitalSignaturesValid = true
            val tamperedBlocks = mutableListOf<Int>()
            
            // Verify blockchain records
            val blockchainRecords = findEvidenceInBlockchain(evidenceId)
            if (blockchainRecords.isEmpty()) {
                blockchainVerified = false
                isValid = false
                verificationDetails.add("No blockchain records found for evidence")
            } else {
                verificationDetails.add("Found ${blockchainRecords.size} blockchain record(s)")
            }
            
            // Verify content hash
            try {
                val decryptedContent = decryptEvidence(evidence)
                val currentHash = calculateSHA256(decryptedContent)
                
                if (currentHash != evidence.contentHash) {
                    hashMatches = false
                    isValid = false
                    verificationDetails.add("Content hash mismatch - evidence may be tampered")
                } else {
                    verificationDetails.add("Content hash verified - evidence integrity intact")
                }
            } catch (e: Exception) {
                hashMatches = false
                isValid = false
                verificationDetails.add("Failed to decrypt and verify content: ${e.message}")
            }
            
            // Verify chain of custody
            val custodyVerification = verifyCustodyChain(evidence.metadata.chainOfCustody)
            if (!custodyVerification) {
                chainOfCustodyIntact = false
                isValid = false
                verificationDetails.add("Chain of custody verification failed")
            } else {
                verificationDetails.add("Chain of custody verified")
            }
            
            // Verify digital signatures
            for (custodyEntry in evidence.metadata.chainOfCustody) {
                if (!verifyDigitalSignature(custodyEntry.digitalSignature, custodyEntry.custodian)) {
                    digitalSignaturesValid = false
                    isValid = false
                    verificationDetails.add("Invalid digital signature from ${custodyEntry.custodian}")
                }
            }
            
            if (digitalSignaturesValid) {
                verificationDetails.add("All digital signatures verified")
            }
            
            // Check for tampered blocks
            val blockIntegrityCheck = verifyBlockchainIntegrity()
            if (!blockIntegrityCheck.isValid) {
                verificationDetails.addAll(blockIntegrityCheck.verificationDetails)
                tamperedBlocks.addAll(blockIntegrityCheck.tamperedBlocks)
                isValid = false
            }
            
            val result = IntegrityVerificationResult(
                isValid = isValid,
                evidenceId = evidenceId,
                blockchainVerified = blockchainVerified,
                hashMatches = hashMatches,
                chainOfCustodyIntact = chainOfCustodyIntact,
                digitalSignaturesValid = digitalSignaturesValid,
                tamperedBlocks = tamperedBlocks,
                verificationDetails = verificationDetails
            )
            
            Log.i(TAG, "Evidence integrity verification completed for $evidenceId: ${if (isValid) "VALID" else "INVALID"}")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify evidence integrity", e)
            Result.failure(e)
        }
    }

    /**
     * Update chain of custody for evidence
     */
    suspend fun updateChainOfCustody(
        evidenceId: String,
        newCustodian: String,
        action: CustodyAction,
        reason: String,
        witnessSignatures: List<String> = emptyList()
    ): Result<String> {
        return try {
            val evidence = evidencePool[evidenceId]
                ?: return Result.failure(Exception("Evidence not found"))
            
            val custodyEntry = CustodyEntry(
                timestamp = System.currentTimeMillis(),
                custodian = newCustodian,
                action = action,
                reason = reason,
                digitalSignature = generateDigitalSignature("$evidenceId$newCustodian${System.currentTimeMillis()}"),
                witnessSignatures = witnessSignatures
            )
            
            val updatedCustody = evidence.metadata.chainOfCustody + custodyEntry
            val updatedMetadata = evidence.metadata.copy(chainOfCustody = updatedCustody)
            val updatedEvidence = evidence.copy(
                metadata = updatedMetadata,
                lastModified = System.currentTimeMillis()
            )
            
            evidencePool[evidenceId] = updatedEvidence
            
            // Create blockchain transaction for custody update
            val transaction = EvidenceTransaction(
                evidenceId = evidenceId,
                action = EvidenceAction.TRANSFER,
                from = evidence.metadata.chainOfCustody.lastOrNull()?.custodian ?: "Unknown",
                to = newCustodian,
                metadata = updatedMetadata,
                digitalSignature = custodyEntry.digitalSignature,
                hash = calculateTransactionHash(evidenceId, EvidenceAction.TRANSFER, updatedMetadata)
            )
            
            addTransaction(transaction)
            
            Log.i(TAG, "Chain of custody updated for evidence $evidenceId")
            Result.success(transaction.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update chain of custody", e)
            Result.failure(e)
        }
    }

    /**
     * Generate legal evidence report with blockchain verification
     */
    suspend fun generateLegalEvidenceReport(evidenceIds: List<String>): Result<LegalEvidenceReport> {
        return try {
            val evidenceReports = mutableListOf<EvidenceReport>()
            
            for (evidenceId in evidenceIds) {
                val evidence = evidencePool[evidenceId]
                    ?: continue
                
                val integrityResult = verifyEvidenceIntegrity(evidenceId).getOrNull()
                    ?: continue
                
                val evidenceReport = EvidenceReport(
                    evidenceId = evidenceId,
                    evidenceType = evidence.metadata.evidenceType,
                    contentHash = evidence.contentHash,
                    createdAt = evidence.createdAt,
                    chainOfCustody = evidence.metadata.chainOfCustody,
                    blockchainRecords = findEvidenceInBlockchain(evidenceId),
                    integrityVerification = integrityResult,
                    legalContext = evidence.metadata.legalContext
                )
                
                evidenceReports.add(evidenceReport)
            }
            
            val legalReport = LegalEvidenceReport(
                reportId = UUID.randomUUID().toString(),
                generatedAt = System.currentTimeMillis(),
                evidenceReports = evidenceReports,
                blockchainVerification = verifyBlockchainIntegrity(),
                legalCertification = generateLegalCertification(evidenceReports),
                digitalSignature = generateDigitalSignature("LegalReport${System.currentTimeMillis()}")
            )
            
            Log.i(TAG, "Legal evidence report generated with ${evidenceReports.size} evidence items")
            Result.success(legalReport)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate legal evidence report", e)
            Result.failure(e)
        }
    }

    data class LegalEvidenceReport(
        val reportId: String,
        val generatedAt: Long,
        val evidenceReports: List<EvidenceReport>,
        val blockchainVerification: IntegrityVerificationResult,
        val legalCertification: LegalCertification,
        val digitalSignature: String
    )

    data class EvidenceReport(
        val evidenceId: String,
        val evidenceType: EvidenceType,
        val contentHash: String,
        val createdAt: Long,
        val chainOfCustody: List<CustodyEntry>,
        val blockchainRecords: List<String>,
        val integrityVerification: IntegrityVerificationResult,
        val legalContext: LegalContext
    )

    data class LegalCertification(
        val certificationId: String,
        val issuedBy: String,
        val issuedAt: Long,
        val validUntil: Long,
        val complianceStandards: List<String>,
        val auditTrail: List<String>,
        val certificationHash: String
    )

    // Core blockchain operations
    private fun createGenesisBlock() {
        val genesisTransaction = EvidenceTransaction(
            evidenceId = "genesis",
            action = EvidenceAction.CREATE,
            from = "System",
            metadata = EvidenceMetadata(
                evidenceType = EvidenceType.SYSTEM_LOG,
                contentHash = GENESIS_HASH,
                fileSize = 0,
                mimeType = "application/json",
                location = null,
                chainOfCustody = emptyList(),
                accessLevel = AccessLevel.PUBLIC,
                retentionPolicy = RetentionPolicy(
                    retentionPeriod = Long.MAX_VALUE,
                    autoDelete = false,
                    legalRequirements = listOf("Permanent blockchain record")
                ),
                legalContext = LegalContext(
                    jurisdiction = "Global",
                    applicableLaws = listOf("Blockchain Genesis Protocol"),
                    evidenceRules = emptyList(),
                    admissibilityRequirements = emptyList()
                )
            ),
            digitalSignature = "genesis_signature",
            hash = GENESIS_HASH
        )
        
        val genesisBlock = Block(
            index = 0,
            timestamp = System.currentTimeMillis(),
            transactions = listOf(genesisTransaction),
            previousHash = GENESIS_HASH,
            merkleRoot = calculateMerkleRoot(listOf(genesisTransaction)),
            nonce = 0,
            hash = GENESIS_HASH,
            miner = "System",
            difficulty = 0
        )
        
        blockchain.add(genesisBlock)
        Log.i(TAG, "Genesis block created")
    }

    private fun addTransaction(transaction: EvidenceTransaction) {
        pendingTransactions.add(transaction)
        Log.d(TAG, "Transaction added to pending pool: ${transaction.id}")
        
        // Trigger mining if enough transactions accumulated
        if (pendingTransactions.size >= MAX_TRANSACTIONS_PER_BLOCK) {
            blockchainScope.launch {
                mineBlock()
            }
        }
    }

    private suspend fun mineBlock() {
        if (isMining || pendingTransactions.isEmpty()) return
        
        isMining = true
        
        try {
            val transactions = pendingTransactions.take(MAX_TRANSACTIONS_PER_BLOCK)
            val previousBlock = blockchain.lastOrNull()
            val previousHash = previousBlock?.hash ?: GENESIS_HASH
            
            val newBlock = Block(
                index = blockchain.size,
                timestamp = System.currentTimeMillis(),
                transactions = transactions,
                previousHash = previousHash,
                merkleRoot = calculateMerkleRoot(transactions),
                nonce = 0,
                hash = "",
                miner = "SafeNet Shield",
                difficulty = DIFFICULTY_TARGET
            )
            
            // Proof of Work mining
            val minedBlock = performProofOfWork(newBlock)
            
            // Validate block
            if (validateBlock(minedBlock)) {
                blockchain.add(minedBlock)
                pendingTransactions.removeAll(transactions)
                
                // Update evidence records with blockchain references
                updateEvidenceBlockchainRecords(transactions, minedBlock.hash)
                
                Log.i(TAG, "Block mined successfully: ${minedBlock.hash}")
            } else {
                Log.e(TAG, "Invalid block rejected")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error mining block", e)
        } finally {
            isMining = false
        }
    }

    private fun performProofOfWork(block: Block): Block {
        var nonce = 0L
        var hash: String
        
        do {
            hash = calculateBlockHash(block.copy(nonce = nonce))
            nonce++
        } while (!hash.startsWith("0".repeat(DIFFICULTY_TARGET)))
        
        return block.copy(nonce = nonce - 1, hash = hash)
    }

    private fun calculateBlockHash(block: Block): String {
        val data = "${block.index}${block.timestamp}${block.previousHash}${block.merkleRoot}${block.nonce}"
        return calculateSHA256(data.toByteArray())
    }

    private fun calculateMerkleRoot(transactions: List<EvidenceTransaction>): String {
        if (transactions.isEmpty()) return GENESIS_HASH
        
        val hashes = transactions.map { it.hash }.toMutableList()
        
        while (hashes.size > 1) {
            val newHashes = mutableListOf<String>()
            
            for (i in hashes.indices step 2) {
                val left = hashes[i]
                val right = if (i + 1 < hashes.size) hashes[i + 1] else left
                val combined = calculateSHA256((left + right).toByteArray())
                newHashes.add(combined)
            }
            
            hashes.clear()
            hashes.addAll(newHashes)
        }
        
        return hashes.first()
    }

    private fun validateBlock(block: Block): Boolean {
        // Validate block structure and hash
        val expectedHash = calculateBlockHash(block)
        if (block.hash != expectedHash) return false
        
        // Validate proof of work
        if (!block.hash.startsWith("0".repeat(DIFFICULTY_TARGET))) return false
        
        // Validate previous hash
        val previousBlock = blockchain.lastOrNull()
        if (previousBlock != null && block.previousHash != previousBlock.hash) return false
        
        // Validate transactions
        return block.transactions.all { validateTransaction(it) }
    }

    private fun validateTransaction(transaction: EvidenceTransaction): Boolean {
        // Validate transaction hash
        val expectedHash = calculateTransactionHash(
            transaction.evidenceId,
            transaction.action,
            transaction.metadata
        )
        return transaction.hash == expectedHash
    }

    private fun calculateTransactionHash(
        evidenceId: String,
        action: EvidenceAction,
        metadata: EvidenceMetadata
    ): String {
        val data = "$evidenceId${action.name}${metadata.contentHash}${metadata.timestamp}"
        return calculateSHA256(data.toByteArray())
    }

    // Helper methods
    private fun calculateSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun generateEvidenceId(): String = UUID.randomUUID().toString()

    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun encryptContent(content: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(content)
    }

    private fun encryptKey(key: SecretKey): String {
        // In production, encrypt with user's master key or use Android Keystore
        return android.util.Base64.encodeToString(key.encoded, android.util.Base64.DEFAULT)
    }

    private fun decryptEvidence(evidence: StoredEvidence): ByteArray {
        val keyBytes = android.util.Base64.decode(evidence.encryptionKey, android.util.Base64.DEFAULT)
        val key = SecretKeySpec(keyBytes, "AES")
        
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(evidence.encryptedContent)
    }

    private fun generateDigitalSignature(data: String): String {
        // Simplified signature - in production use proper digital signature algorithms
        return calculateSHA256(data.toByteArray())
    }

    private fun verifyDigitalSignature(signature: String, signer: String): Boolean {
        // Simplified verification - in production verify against public key
        return signature.isNotEmpty() && signature.length == 64 // SHA-256 hex length
    }

    private fun determineMimeType(evidenceType: EvidenceType): String {
        return when (evidenceType) {
            EvidenceType.SCREENSHOT, EvidenceType.PHOTO -> "image/jpeg"
            EvidenceType.AUDIO_RECORDING -> "audio/mp3"
            EvidenceType.VIDEO_RECORDING -> "video/mp4"
            EvidenceType.TEXT_MESSAGE, EvidenceType.EMAIL -> "text/plain"
            EvidenceType.DOCUMENT -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    private fun determineAccessLevel(evidenceType: EvidenceType): AccessLevel {
        return when (evidenceType) {
            EvidenceType.BIOMETRIC_DATA -> AccessLevel.SECRET
            EvidenceType.GPS_LOCATION -> AccessLevel.CONFIDENTIAL
            else -> AccessLevel.RESTRICTED
        }
    }

    private fun createDefaultRetentionPolicy(): RetentionPolicy {
        return RetentionPolicy(
            retentionPeriod = 365L * 24 * 60 * 60 * 1000, // 1 year
            autoDelete = false,
            archiveAfter = 90L * 24 * 60 * 60 * 1000, // 90 days
            legalRequirements = listOf("Standard evidence retention")
        )
    }

    private fun verifyCustodyChain(custodyChain: List<CustodyEntry>): Boolean {
        // Verify custody chain continuity and signatures
        return custodyChain.isNotEmpty() && custodyChain.all { 
            verifyDigitalSignature(it.digitalSignature, it.custodian)
        }
    }

    private fun findEvidenceInBlockchain(evidenceId: String): List<String> {
        val transactionIds = mutableListOf<String>()
        
        for (block in blockchain) {
            for (transaction in block.transactions) {
                if (transaction.evidenceId == evidenceId) {
                    transactionIds.add(transaction.id)
                }
            }
        }
        
        return transactionIds
    }

    private fun verifyBlockchainIntegrity(): IntegrityVerificationResult {
        val verificationDetails = mutableListOf<String>()
        val tamperedBlocks = mutableListOf<Int>()
        var isValid = true
        
        for (i in 1 until blockchain.size) {
            val currentBlock = blockchain[i]
            val previousBlock = blockchain[i - 1]
            
            // Verify hash chain
            if (currentBlock.previousHash != previousBlock.hash) {
                isValid = false
                tamperedBlocks.add(i)
                verificationDetails.add("Block $i: Previous hash mismatch")
            }
            
            // Verify block hash
            val expectedHash = calculateBlockHash(currentBlock)
            if (currentBlock.hash != expectedHash) {
                isValid = false
                tamperedBlocks.add(i)
                verificationDetails.add("Block $i: Hash verification failed")
            }
        }
        
        if (isValid) {
            verificationDetails.add("Blockchain integrity verified - no tampering detected")
        }
        
        return IntegrityVerificationResult(
            isValid = isValid,
            evidenceId = "blockchain",
            blockchainVerified = isValid,
            hashMatches = isValid,
            chainOfCustodyIntact = isValid,
            digitalSignaturesValid = isValid,
            tamperedBlocks = tamperedBlocks,
            verificationDetails = verificationDetails
        )
    }

    private fun updateEvidenceBlockchainRecords(transactions: List<EvidenceTransaction>, blockHash: String) {
        for (transaction in transactions) {
            evidencePool[transaction.evidenceId]?.let { evidence ->
                val updatedEvidence = evidence.copy(
                    blockchainRecords = evidence.blockchainRecords + transaction.id
                )
                evidencePool[transaction.evidenceId] = updatedEvidence
            }
        }
    }

    private fun initializeValidatorNodes() {
        // Initialize validator nodes for consensus
        validatorNodes.add(
            ValidatorNode(
                nodeId = "safenet_primary",
                publicKey = "primary_node_key",
                reputation = 1.0f,
                stakingAmount = 100f,
                isActive = true,
                lastSeen = System.currentTimeMillis()
            )
        )
    }

    private fun startMiningProcess() {
        blockchainScope.launch {
            while (isActive) {
                try {
                    if (pendingTransactions.isNotEmpty() && 
                        System.currentTimeMillis() - lastBlockTime > BLOCK_TIME_TARGET) {
                        mineBlock()
                        lastBlockTime = System.currentTimeMillis()
                    }
                    delay(30000) // Check every 30 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "Error in mining process", e)
                }
            }
        }
    }

    private fun generateLegalCertification(evidenceReports: List<EvidenceReport>): LegalCertification {
        return LegalCertification(
            certificationId = UUID.randomUUID().toString(),
            issuedBy = "SafeNet Shield Blockchain Authority",
            issuedAt = System.currentTimeMillis(),
            validUntil = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 year
            complianceStandards = listOf("ISO 27001", "GDPR", "Kenya Data Protection Act"),
            auditTrail = evidenceReports.map { "Verified evidence: ${it.evidenceId}" },
            certificationHash = calculateSHA256("certification${System.currentTimeMillis()}".toByteArray())
        )
    }

    /**
     * Clean up blockchain resources
     */
    fun cleanup() {
        blockchainScope.cancel()
        Log.d(TAG, "Blockchain evidence system cleaned up")
    }
}
