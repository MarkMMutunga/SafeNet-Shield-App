package com.safenet.shield.government

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Case Tracking Fragment
 * Allows users to track the status of their government reports
 */
class CaseTrackingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createTrackingView()
    }

    private fun createTrackingView(): View {
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
            text = "Track Your Cases"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(headerText)

        // Search Card
        val searchCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val searchLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val searchTitle = TextView(context).apply {
            text = "Enter Reference ID"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        searchLayout.addView(searchTitle)

        val referenceInput = EditText(context).apply {
            hint = "GOV-1234567890"
            background = context.getDrawable(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        searchLayout.addView(referenceInput)

        val searchButton = MaterialButton(context).apply {
            text = "Track Case"
            setPadding(32, 16, 32, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 0) }
            setOnClickListener {
                trackCase(referenceInput.text.toString())
            }
        }
        searchLayout.addView(searchButton)

        searchCard.addView(searchLayout)
        mainLayout.addView(searchCard)

        // Recent Cases Card
        val recentCasesCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val recentCasesLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val recentTitle = TextView(context).apply {
            text = "Recent Cases"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        recentCasesLayout.addView(recentTitle)

        // Sample cases
        val sampleCases = listOf(
            Triple("GOV-1640123456", "Cybercrime Incident", "Under Investigation"),
            Triple("GOV-1640234567", "Identity Theft", "In Progress"),
            Triple("GOV-1640345678", "Online Fraud", "Closed - Resolved")
        )

        sampleCases.forEach { (id, type, status) ->
            val caseItem = createCaseItem(context, id, type, status)
            recentCasesLayout.addView(caseItem)
        }

        recentCasesCard.addView(recentCasesLayout)
        mainLayout.addView(recentCasesCard)

        // Status Legend Card
        val legendCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val legendLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val legendTitle = TextView(context).apply {
            text = "Status Legend"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        legendLayout.addView(legendTitle)

        val statuses = listOf(
            "🔴 Received - Initial review pending",
            "🟡 Under Investigation - Being processed",
            "🔵 In Progress - Active investigation",
            "🟢 Closed - Resolved - Case completed",
            "⚫ Closed - Unresolved - Insufficient evidence"
        )

        statuses.forEach { status ->
            val statusText = TextView(context).apply {
                text = status
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }
            legendLayout.addView(statusText)
        }

        legendCard.addView(legendLayout)
        mainLayout.addView(legendCard)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createCaseItem(context: android.content.Context, id: String, type: String, status: String): View {
        val caseCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
            cardElevation = 4f
            radius = 8f
            setPadding(16, 16, 16, 16)
        }

        val caseLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val idText = TextView(context).apply {
            text = "ID: $id"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        caseLayout.addView(idText)

        val typeText = TextView(context).apply {
            text = "Type: $type"
            textSize = 12f
            setPadding(0, 4, 0, 4)
        }
        caseLayout.addView(typeText)

        val statusText = TextView(context).apply {
            text = "Status: $status"
            textSize = 12f
            val color = when (status) {
                "Under Investigation" -> android.graphics.Color.parseColor("#FF9800")
                "In Progress" -> android.graphics.Color.parseColor("#2196F3")
                "Closed - Resolved" -> android.graphics.Color.parseColor("#4CAF50")
                else -> android.graphics.Color.parseColor("#757575")
            }
            setTextColor(color)
        }
        caseLayout.addView(statusText)

        caseCard.addView(caseLayout)

        caseCard.setOnClickListener {
            showCaseDetails(id, type, status)
        }

        return caseCard
    }

    private fun trackCase(referenceId: String) {
        if (referenceId.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a reference ID", Toast.LENGTH_SHORT).show()
            return
        }

        if (!referenceId.startsWith("GOV-")) {
            Toast.makeText(requireContext(), "Invalid reference ID format", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate case lookup
        val statuses = listOf(
            "Received - Initial review pending",
            "Under Investigation - Being processed",
            "In Progress - Active investigation",
            "Closed - Resolved"
        )
        val randomStatus = statuses.random()
        
        Toast.makeText(
            requireContext(),
            "Case $referenceId found.\nStatus: $randomStatus",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showCaseDetails(id: String, type: String, status: String) {
        val message = """
            Case Details:
            
            Reference ID: $id
            Case Type: $type
            Current Status: $status
            
            Last Updated: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date())}
            
            For more details, contact the relevant government agency directly.
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Case Information")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
