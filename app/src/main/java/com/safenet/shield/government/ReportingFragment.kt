package com.safenet.shield.government

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Government Reporting Fragment
 * Allows users to file official reports with government agencies
 */
class ReportingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createReportingView()
    }

    private fun createReportingView(): View {
        val context = requireContext()
        
        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Header
        val headerText = TextView(context).apply {
            text = "File Official Report"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(headerText)

        // Report Type Card
        val reportTypeCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val reportTypeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val reportTypeTitle = TextView(context).apply {
            text = "Report Type"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        reportTypeLayout.addView(reportTypeTitle)

        val reportTypeSpinner = Spinner(context).apply {
            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                listOf(
                    "Cybercrime Incident",
                    "Identity Theft",
                    "Online Fraud",
                    "Data Breach",
                    "Digital Harassment",
                    "Financial Crime",
                    "Other Criminal Activity"
                )
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
        }
        reportTypeLayout.addView(reportTypeSpinner)

        reportTypeCard.addView(reportTypeLayout)
        mainLayout.addView(reportTypeCard)

        // Incident Details Card
        val incidentCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val incidentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val incidentTitle = TextView(context).apply {
            text = "Incident Details"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        incidentLayout.addView(incidentTitle)

        val incidentDescription = EditText(context).apply {
            hint = "Detailed description of the incident..."
            minLines = 4
            maxLines = 8
            background = context.getDrawable(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        incidentLayout.addView(incidentDescription)

        incidentCard.addView(incidentLayout)
        mainLayout.addView(incidentCard)

        // Evidence Card
        val evidenceCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val evidenceLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val evidenceTitle = TextView(context).apply {
            text = "Evidence & Documentation"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        evidenceLayout.addView(evidenceTitle)

        val attachFileButton = MaterialButton(context).apply {
            text = "Attach Files"
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                Toast.makeText(context, "File attachment functionality coming soon", Toast.LENGTH_SHORT).show()
            }
        }
        evidenceLayout.addView(attachFileButton)

        val evidenceNote = TextView(context).apply {
            text = "Supported formats: PDF, DOC, JPG, PNG, MP4 (Max 50MB)"
            textSize = 12f
            alpha = 0.7f
            setPadding(0, 8, 0, 0)
        }
        evidenceLayout.addView(evidenceNote)

        evidenceCard.addView(evidenceLayout)
        mainLayout.addView(evidenceCard)

        // Contact Information Card
        val contactCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val contactLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val contactTitle = TextView(context).apply {
            text = "Contact Information"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        contactLayout.addView(contactTitle)

        val phoneInput = EditText(context).apply {
            hint = "Phone Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            background = context.getDrawable(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        contactLayout.addView(phoneInput)

        val emailInput = EditText(context).apply {
            hint = "Email Address"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            background = context.getDrawable(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 0) }
        }
        contactLayout.addView(emailInput)

        contactCard.addView(contactLayout)
        mainLayout.addView(contactCard)

        // Submit Button
        val submitButton = MaterialButton(context).apply {
            text = "Submit Official Report"
            textSize = 16f
            setPadding(48, 24, 48, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 24, 0, 32) }
            setOnClickListener {
                submitReport(reportTypeSpinner, incidentDescription, phoneInput, emailInput)
            }
        }
        mainLayout.addView(submitButton)

        // Disclaimer
        val disclaimer = TextView(context).apply {
            text = "⚠️ This report will be submitted to relevant government agencies. False reporting is a criminal offense."
            textSize = 12f
            alpha = 0.8f
            setPadding(16, 0, 16, 0)
        }
        mainLayout.addView(disclaimer)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun submitReport(
        reportType: Spinner,
        description: EditText,
        phone: EditText,
        email: EditText
    ) {
        val selectedType = reportType.selectedItem.toString()
        val descriptionText = description.text.toString()
        val phoneText = phone.text.toString()
        val emailText = email.text.toString()

        if (descriptionText.isBlank()) {
            Toast.makeText(requireContext(), "Please provide incident description", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneText.isBlank() && emailText.isBlank()) {
            Toast.makeText(requireContext(), "Please provide contact information", Toast.LENGTH_SHORT).show()
            return
        }

        // In a real app, this would submit to government APIs
        Toast.makeText(
            requireContext(),
            "Report submitted successfully. Reference ID: GOV-${System.currentTimeMillis()}",
            Toast.LENGTH_LONG
        ).show()

        // Clear form
        description.text.clear()
        phone.text.clear()
        email.text.clear()
        reportType.setSelection(0)
    }
}
