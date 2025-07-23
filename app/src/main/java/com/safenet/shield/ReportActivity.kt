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

package com.safenet.shield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.safenet.shield.databinding.ActivityReportBinding
import com.safenet.shield.databinding.ItemAttachmentBinding
import com.safenet.shield.data.LocationData
import com.safenet.shield.utils.ValidationUtils
import com.safenet.shield.cybercrime.CybercrimeReportingSystem
import com.safenet.shield.cybercrime.EvidenceManager
import com.safenet.shield.cybercrime.MpesaScamDetector
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private val TAG = "ReportActivity"
    
    // Enhanced cybercrime reporting components
    private lateinit var cybercrimeSystem: CybercrimeReportingSystem
    private lateinit var evidenceManager: EvidenceManager
    private lateinit var mpesaScamDetector: MpesaScamDetector
    private var selectedCybercrimeType: CybercrimeReportingSystem.CybercrimeType? = null
    private var selectedCountry: String? = null
    private val attachments = mutableListOf<Attachment>()
    private lateinit var attachmentsAdapter: AttachmentsAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permissions required to attach files", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.get("data")?.let { data ->
                if (data is android.graphics.Bitmap) {
                    saveBitmapToFile(data)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize cybercrime components
        cybercrimeSystem = CybercrimeReportingSystem(this)
        evidenceManager = EvidenceManager(this)
        mpesaScamDetector = MpesaScamDetector(this)
        
        setupUI()
        setupCybercrimeTypes()
    }

    private fun setupUI() {
        try {
            // Set up cybercrime type selection
            setupCybercrimeTypeSelection()
            
            // Set up attachments RecyclerView
            attachmentsAdapter = AttachmentsAdapter(attachments) { position ->
                attachments.removeAt(position)
                attachmentsAdapter.notifyItemRemoved(position)
            }
            binding.attachmentsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ReportActivity)
                adapter = attachmentsAdapter
            }

            // Set up attachment buttons with enhanced evidence management
            binding.attachImageButton.setOnClickListener {
                checkPermissionsAndOpenPicker("image/*")
            }

            binding.attachDocumentButton.setOnClickListener {
                checkPermissionsAndOpenPicker("application/pdf")
            }

            binding.takePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                }
            }

            // Set up country dropdown
            val countryAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                LocationData.countries.map { it.name }
            )
            binding.countryDropdown.setAdapter(countryAdapter)

            // Set up city dropdown (initially empty)
            val cityAdapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                mutableListOf()
            )
            binding.cityDropdown.setAdapter(cityAdapter)

            // Set up country code dropdown
            val countryCodeAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                LocationData.countries.map { "${it.name} (${it.countryCode})" }
            )
            binding.countryCodeDropdown.setAdapter(countryCodeAdapter)

            // Handle country selection
            binding.countryDropdown.setOnItemClickListener { _, _, position, _ ->
                selectedCountry = LocationData.countries[position].name
                // Update cities based on selected country
                val cities = LocationData.countries[position].cities
                cityAdapter.clear()
                cityAdapter.addAll(cities)
                cityAdapter.notifyDataSetChanged()
                // Clear city selection when country changes
                binding.cityDropdown.text.clear()
                // Update country code
                binding.countryCodeDropdown.setText("${LocationData.countries[position].name} (${LocationData.countries[position].countryCode})")
            }

            // Set up submit button
            binding.submitButton.setOnClickListener {
                submitReport()
            }

            // Set up emergency contacts button
            binding.emergencyButton.setOnClickListener {
                startActivity(Intent(this, EmergencyContactsActivity::class.java))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            Toast.makeText(this, "Error setting up UI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCybercrimeTypes() {
        // Add cybercrime type chips to the UI
        val cybercrimeTypes = CybercrimeReportingSystem.CybercrimeType.values()
        
        // This would be added to a ChipGroup in the layout
        // For now, we'll use the existing incident type field
        val typeNames = cybercrimeTypes.map { type ->
            type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        }
        
        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            typeNames
        )
        // Assuming we add this to an existing dropdown or create a new one
    }

    private fun setupCybercrimeTypeSelection() {
        // Quick access buttons for common cybercrime types
        // These would be implemented as chips or buttons in the actual UI
        
        // For M-Pesa scam detection
        binding.incidentTypeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val title = binding.incidentTypeInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                
                // Check if this might be an M-Pesa scam
                if (title.lowercase().contains("mpesa") || 
                    description.lowercase().contains("mpesa")) {
                    suggestMpesaScamDetection(description)
                }
            }
        }
    }

    private fun suggestMpesaScamDetection(message: String) {
        val analysis = mpesaScamDetector.analyzeSmsMessage(message)
        
        if (analysis.isLikelyScam && analysis.confidenceLevel > 0.6) {
            AlertDialog.Builder(this)
                .setTitle("Possible M-Pesa Scam Detected")
                .setMessage("Our system detected this might be an M-Pesa scam with ${(analysis.confidenceLevel * 100).toInt()}% confidence. Would you like to use the M-Pesa scam reporting template?")
                .setPositiveButton("Use Template") { _, _ ->
                    loadMpesaScamTemplate(analysis)
                }
                .setNegativeButton("Continue Manually", null)
                .show()
        }
    }

    private fun loadMpesaScamTemplate(analysis: MpesaScamDetector.ScamAnalysis) {
        selectedCybercrimeType = CybercrimeReportingSystem.CybercrimeType.MPESA_SCAM
        val template = cybercrimeSystem.createQuickReportTemplate(selectedCybercrimeType!!)
        
        // Update UI with template information
        binding.incidentTypeInput.setText(template.title)
        
        // Add detected risk factors to description
        val descriptionBuilder = StringBuilder()
        descriptionBuilder.appendLine("DETECTED SCAM PATTERNS:")
        analysis.riskFactors.forEach { factor ->
            descriptionBuilder.appendLine("• ${factor.description}")
        }
        descriptionBuilder.appendLine("\nORIGINAL DESCRIPTION:")
        descriptionBuilder.append(binding.descriptionInput.text.toString())
        
        binding.descriptionInput.setText(descriptionBuilder.toString())
        
        // Show recommendations
        showScamRecommendations(analysis.recommendations)
    }

    private fun showScamRecommendations(recommendations: List<String>) {
        val recommendationText = recommendations.joinToString("\n• ", "RECOMMENDATIONS:\n• ")
        
        AlertDialog.Builder(this)
            .setTitle("Security Recommendations")
            .setMessage(recommendationText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadCybercrimeTemplate(type: CybercrimeReportingSystem.CybercrimeType) {
        selectedCybercrimeType = type
        val template = cybercrimeSystem.createQuickReportTemplate(type)
        
        // Update title
        binding.incidentTypeInput.setText(template.title)
        
        // Create guided questions dialog
        showGuidedQuestionsDialog(template)
    }

    private fun showGuidedQuestionsDialog(template: CybercrimeReportingSystem.ReportTemplate) {
        // This would create a multi-step dialog for guided questions
        // For now, we'll show the evidence requirements
        
        val evidenceText = template.requiredEvidence.joinToString("\n• ", "REQUIRED EVIDENCE:\n• ")
        val actionsText = template.suggestedActions.joinToString("\n• ", "\n\nSUGGESTED ACTIONS:\n• ")
        
        AlertDialog.Builder(this)
            .setTitle("${template.title} - Guidance")
            .setMessage(evidenceText + actionsText)
            .setPositiveButton("Continue", null)
            .show()
    }

    private fun checkPermissionsAndOpenPicker(mimeType: String) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
        requestPermissionLauncher.launch(permissions)
    }

    private fun openFilePicker(mimeType: String = "*/*") {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
        }
        pickFileLauncher.launch(intent)
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            val fileName = getFileName(uri)
            val fileType = getFileType(fileName)
            
            // Enhanced evidence processing with security analysis
            val mimeType = contentResolver.getType(uri) ?: "unknown"
            
            // Check if it's a screenshot that might need analysis
            if (mimeType.startsWith("image/")) {
                analyzeImageEvidence(uri, fileName, fileType)
            } else {
                storeEvidenceSecurely(uri, fileName, fileType, "Document evidence")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling selected file", e)
            Toast.makeText(this, "Error processing file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImageEvidence(uri: Uri, fileName: String, fileType: AttachmentType) {
        try {
            // Load bitmap for analysis
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val analysis = evidenceManager.analyzeScreenshot(bitmap)
            
            if (analysis.hasWarnings) {
                // Show warning dialog with suggestions
                val warningMessage = analysis.warnings.joinToString("\n• ", "PRIVACY WARNINGS:\n• ") +
                    "\n\n" + analysis.suggestions.joinToString("\n• ", "SUGGESTIONS:\n• ")
                
                AlertDialog.Builder(this)
                    .setTitle("Screenshot Privacy Check")
                    .setMessage(warningMessage)
                    .setPositiveButton("Continue Anyway") { _, _ ->
                        storeEvidenceSecurely(uri, fileName, fileType, "Screenshot evidence (privacy reviewed)")
                    }
                    .setNegativeButton("Choose Different Image") { _, _ ->
                        // Do nothing, let user select a different image
                    }
                    .show()
            } else {
                storeEvidenceSecurely(uri, fileName, fileType, "Screenshot evidence")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image evidence", e)
            // Fallback to normal processing
            storeEvidenceSecurely(uri, fileName, fileType, "Image evidence")
        }
    }

    private fun storeEvidenceSecurely(uri: Uri, fileName: String, fileType: AttachmentType, description: String) {
        // Store evidence using the enhanced evidence manager
        val storeResult = evidenceManager.storeEvidence(
            uri = uri,
            description = description,
            isAnonymous = false // This could be based on user preference
        )
        
        storeResult.onSuccess { metadata ->
            // Create attachment with evidence ID
            val attachment = Attachment(fileName, uri, fileType, metadata.id)
            attachments.add(attachment)
            attachmentsAdapter.notifyItemInserted(attachments.size - 1)
            
            Toast.makeText(this, "Evidence secured: ${metadata.id}", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Evidence stored with ID: ${metadata.id}")
            
        }.onFailure { exception ->
            Log.e(TAG, "Failed to store evidence securely", exception)
            // Fallback to basic attachment handling
            val attachment = Attachment(fileName, uri, fileType)
            attachments.add(attachment)
            attachmentsAdapter.notifyItemInserted(attachments.size - 1)
            
            Toast.makeText(this, "File attached (basic mode)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: android.graphics.Bitmap) {
        try {
            val file = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this, "${packageName}.provider", file
            )
            handleSelectedFile(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap", e)
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "attachment_${System.currentTimeMillis()}"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
        }
        return fileName
    }

    private fun getFileType(fileName: String): AttachmentType {
        return when (fileName.substringAfterLast(".", "").lowercase()) {
            "jpg", "jpeg", "png", "gif" -> AttachmentType.IMAGE
            "pdf", "doc", "docx", "txt" -> AttachmentType.DOCUMENT
            else -> AttachmentType.OTHER
        }
    }

    private fun submitReport() {
        try {
            val incidentType = binding.incidentTypeInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()
            val country = binding.countryDropdown.text.toString().trim()
            val city = binding.cityDropdown.text.toString().trim()
            val contact = binding.contactInput.text.toString().trim()
            val countryCode = binding.countryCodeDropdown.text.toString().trim()

            // Enhanced validation using ValidationUtils
            if (incidentType.isEmpty()) {
                binding.incidentTypeInput.error = "Incident type is required"
                return
            }

            if (!ValidationUtils.isValidReportContent(description)) {
                binding.descriptionInput.error = "Please provide a detailed description (10-5000 characters)"
                return
            }

            // Validate location
            if (country.isEmpty()) {
                Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
                return
            }

            // Validate contact if provided
            if (contact.isNotEmpty() && !ValidationUtils.isValidPhoneNumber(contact)) {
                binding.contactInput.error = "Please enter a valid phone number"
                return
            }

            // Security: Sanitize inputs and check for injection attempts
            val sanitizedInputs = listOf(incidentType, description, country, city, contact)
            if (sanitizedInputs.any { ValidationUtils.containsSqlInjection(it) }) {
                Toast.makeText(this, "Invalid input detected", Toast.LENGTH_SHORT).show()
                return
            }

            // Clear errors
            binding.incidentTypeInput.error = null
            binding.descriptionInput.error = null
            binding.contactInput.error = null

            // Format phone number if provided
            val formattedContact = if (contact.isNotEmpty() && countryCode.isNotEmpty()) {
                val code = countryCode.substringAfterLast("(").substringBefore(")")
                "$code$contact"
            } else {
                contact
            }

            // Get current user
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            // Create report data with sanitized inputs
            val report = hashMapOf(
                "title" to ValidationUtils.sanitizeInput(incidentType),
                "description" to ValidationUtils.sanitizeInput(description),
                "country" to ValidationUtils.sanitizeInput(country),
                "city" to ValidationUtils.sanitizeInput(city),
                "contact" to formattedContact,
                "timestamp" to System.currentTimeMillis(),
                "userId" to currentUser.uid,
                "status" to "pending"
            )

            // Add report to Firestore
            FirebaseFirestore.getInstance().collection("reports")
                .add(report)
                .addOnSuccessListener { documentReference ->
                    // Handle attachments if any
                    if (attachments.isNotEmpty()) {
                        uploadAttachments(documentReference.id)
                    } else {
                        // No attachments, show success message
                        Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding report", e)
                    Toast.makeText(this, "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error in submitReport", e)
            Toast.makeText(this, "Error submitting report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAttachments(reportId: String) {
        try {
            val storageRef = FirebaseStorage.getInstance().reference
            var uploadedCount = 0
            val totalAttachments = attachments.size

            attachments.forEach { attachment ->
                // Validate file type for security
                val allowedTypes = listOf("jpg", "jpeg", "png", "pdf", "doc", "docx")
                if (!ValidationUtils.isValidFileType(attachment.name, allowedTypes)) {
                    Toast.makeText(this, "File type ${attachment.name} not allowed", Toast.LENGTH_SHORT).show()
                    return
                }

                val fileRef = storageRef.child("reports/$reportId/${attachment.name}")
                contentResolver.openInputStream(attachment.uri)?.use { inputStream ->
                    fileRef.putStream(inputStream)
                        .addOnSuccessListener {
                            uploadedCount++
                            if (uploadedCount == totalAttachments) {
                                Toast.makeText(this, "Report submitted successfully with attachments", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error uploading attachment: ${attachment.name}", e)
                            uploadedCount++
                            if (uploadedCount == totalAttachments) {
                                Toast.makeText(this, "Report submitted with some upload failures", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading attachments", e)
            Toast.makeText(this, "Error uploading attachments", Toast.LENGTH_SHORT).show()
        }
    }

    data class Attachment(
        val name: String,
        val uri: Uri,
        val type: AttachmentType,
        val evidenceId: String? = null // Enhanced evidence tracking
    )

    enum class AttachmentType {
        IMAGE, DOCUMENT, OTHER
    }

    inner class AttachmentsAdapter(
        private val items: MutableList<Attachment>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<AttachmentsAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(attachment: Attachment) {
                binding.attachmentName.text = attachment.name
                // Remove the attachmentType line as it doesn't exist in the layout
                binding.removeButton.setOnClickListener {
                    onRemove(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAttachmentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size
    }
}
