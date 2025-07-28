package com.safenet.shield.government

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

/**
 * Compliance Fragment
 * Shows legal compliance and regulatory information
 */
class ComplianceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createComplianceView()
    }

    private fun createComplianceView(): View {
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
            text = "Legal Compliance & Regulations"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(headerText)

        // Kenyan Laws Card
        val lawsCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val lawsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val lawsTitle = TextView(context).apply {
            text = "🇰🇪 Kenyan Cybersecurity Laws"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        lawsLayout.addView(lawsTitle)

        val laws = listOf(
            "• Computer Misuse and Cybercrimes Act, 2018",
            "• Data Protection Act, 2019",
            "• Kenya Information and Communications Act",
            "• Central Bank of Kenya (CBK) Guidelines",
            "• Communications Authority Regulations"
        )

        laws.forEach { law ->
            val lawText = TextView(context).apply {
                text = law
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }
            lawsLayout.addView(lawText)
        }

        lawsCard.addView(lawsLayout)
        mainLayout.addView(lawsCard)

        // Reporting Requirements Card
        val requirementsCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val requirementsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val requirementsTitle = TextView(context).apply {
            text = "📋 Mandatory Reporting Requirements"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        requirementsLayout.addView(requirementsTitle)

        val requirements = listOf(
            "🔴 Critical Infrastructure Attacks - Immediate",
            "🟡 Data Breaches affecting >1000 users - 72 hours", 
            "🔵 Financial Cybercrimes - 24 hours",
            "⚪ Other Cybersecurity Incidents - 7 days"
        )

        requirements.forEach { requirement ->
            val reqText = TextView(context).apply {
                text = requirement
                textSize = 14f
                setPadding(0, 6, 0, 6)
            }
            requirementsLayout.addView(reqText)
        }

        requirementsCard.addView(requirementsLayout)
        mainLayout.addView(requirementsCard)

        // Penalties Card
        val penaltiesCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val penaltiesLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val penaltiesTitle = TextView(context).apply {
            text = "⚖️ Legal Penalties & Sanctions"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        penaltiesLayout.addView(penaltiesTitle)

        val penalties = listOf(
            "Cybercrime: Up to KES 20M fine or 20 years imprisonment",
            "Data Protection violations: Up to KES 5M fine",
            "Computer misuse: Up to KES 200K fine or 2 years",
            "False reporting: Criminal charges and civil liability"
        )

        penalties.forEach { penalty ->
            val penaltyText = TextView(context).apply {
                text = "• $penalty"
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }
            penaltiesLayout.addView(penaltyText)
        }

        penaltiesCard.addView(penaltiesLayout)
        mainLayout.addView(penaltiesCard)

        // Compliance Checklist Card
        val checklistCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }

        val checklistLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val checklistTitle = TextView(context).apply {
            text = "✅ Compliance Checklist"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        checklistLayout.addView(checklistTitle)

        val checklistItems = listOf(
            "Register with Data Protection Office",
            "Implement incident response procedures",
            "Train staff on cybersecurity policies",
            "Conduct regular security assessments",
            "Maintain incident logs and documentation",
            "Establish legal reporting protocols"
        )

        checklistItems.forEach { item ->
            val checkBox = CheckBox(context).apply {
                text = item
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }
            checklistLayout.addView(checkBox)
        }

        checklistCard.addView(checklistLayout)
        mainLayout.addView(checklistCard)

        // Disclaimer
        val disclaimer = TextView(context).apply {
            text = "⚠️ Disclaimer: This information is for general guidance only. Consult qualified legal professionals for specific compliance requirements."
            textSize = 12f
            alpha = 0.8f
            setPadding(16, 16, 16, 16)
            background = context.getDrawable(android.R.drawable.editbox_background)
        }
        mainLayout.addView(disclaimer)

        scrollView.addView(mainLayout)
        return scrollView
    }
}
