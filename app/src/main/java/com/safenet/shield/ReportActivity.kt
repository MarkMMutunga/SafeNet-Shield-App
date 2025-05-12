package com.safenet.shield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safenet.shield.databinding.ActivityReportBinding
import com.safenet.shield.databinding.ItemAttachmentBinding
import com.safenet.shield.data.LocationData
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private val TAG = "ReportActivity"
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

        setupUI()
    }

    private fun setupUI() {
        try {
            // Set up attachments RecyclerView
            attachmentsAdapter = AttachmentsAdapter(attachments) { position ->
                attachments.removeAt(position)
                attachmentsAdapter.notifyItemRemoved(position)
            }
            binding.attachmentsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ReportActivity)
                adapter = attachmentsAdapter
            }

            // Set up attachment buttons
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
            Toast.makeText(this, "Error setting up the form", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndOpenPicker(mimeType: String) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        
        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            openFilePicker(mimeType)
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun openFilePicker(mimeType: String = "*/*") {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_OPENABLE)
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
            attachments.add(Attachment(fileName, uri, fileType))
            attachmentsAdapter.notifyItemInserted(attachments.size - 1)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling selected file", e)
            Toast.makeText(this, "Error attaching file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: android.graphics.Bitmap) {
        try {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            attachments.add(Attachment(fileName, Uri.fromFile(file), AttachmentType.IMAGE))
            attachmentsAdapter.notifyItemInserted(attachments.size - 1)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo", e)
            Toast.makeText(this, "Error saving photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "Unknown file"
    }

    private fun getFileType(fileName: String): AttachmentType {
        return when (fileName.substringAfterLast('.').lowercase()) {
            "jpg", "jpeg", "png", "gif" -> AttachmentType.IMAGE
            "pdf" -> AttachmentType.PDF
            else -> AttachmentType.OTHER
        }
    }

    private fun submitReport() {
        try {
            val incidentType = binding.incidentTypeInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val country = binding.countryDropdown.text.toString()
            val city = binding.cityDropdown.text.toString()
            val contact = binding.contactInput.text.toString()
            val countryCode = binding.countryCodeDropdown.text.toString()

            // Basic validation
            if (incidentType.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return
            }

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

            // Create report data
            val report = hashMapOf(
                "title" to incidentType,
                "description" to description,
                "country" to country,
                "city" to city,
                "contact" to formattedContact,
                "timestamp" to System.currentTimeMillis(),
                "userId" to currentUser.uid
            )

            // Add report to Firestore
            FirebaseFirestore.getInstance().collection("reports")
                .add(report)
                .addOnSuccessListener { documentReference ->
                    // Handle attachments if any
                    if (attachments.isNotEmpty()) {
                        val storageRef = FirebaseStorage.getInstance().reference
                        attachments.forEach { attachment ->
                            val fileRef = storageRef.child("reports/${documentReference.id}/${attachment.name}")
                            contentResolver.openInputStream(attachment.uri)?.use { inputStream ->
                                fileRef.putStream(inputStream)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Attachment uploaded successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error uploading attachment", e)
                                    }
                            }
                        }
                    }

                    // Show success message
                    val location = if (country.isNotEmpty() && city.isNotEmpty()) {
                        "$city, $country"
                    } else if (country.isNotEmpty()) {
                        country
                    } else {
                        "Location not specified"
                    }
                    
                    val attachmentCount = attachments.size
                    Toast.makeText(this, 
                        "Report submitted successfully from $location with $attachmentCount attachments", 
                        Toast.LENGTH_SHORT).show()
                    
                    // Clear the form
                    binding.incidentTypeInput.text?.clear()
                    binding.descriptionInput.text?.clear()
                    binding.countryDropdown.text?.clear()
                    binding.cityDropdown.text?.clear()
                    binding.countryCodeDropdown.text?.clear()
                    binding.contactInput.text?.clear()
                    attachments.clear()
                    attachmentsAdapter.notifyDataSetChanged()

                    // Navigate back to main activity
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error submitting report", e)
                    Toast.makeText(this, "Error submitting report: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error submitting report", e)
            Toast.makeText(this, "Error submitting report", Toast.LENGTH_SHORT).show()
        }
    }

    data class Attachment(
        val name: String,
        val uri: Uri,
        val type: AttachmentType
    )

    enum class AttachmentType {
        IMAGE, PDF, OTHER
    }

    inner class AttachmentsAdapter(
        private val items: List<Attachment>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<AttachmentsAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemAttachmentBinding) : 
            RecyclerView.ViewHolder(binding.root) {
            
            fun bind(attachment: Attachment) {
                binding.attachmentName.text = attachment.name
                binding.attachmentIcon.setImageResource(R.drawable.ic_attachment)
                binding.removeButton.setOnClickListener {
                    onRemove(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAttachmentBinding.inflate(
                layoutInflater, parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size
    }
} 