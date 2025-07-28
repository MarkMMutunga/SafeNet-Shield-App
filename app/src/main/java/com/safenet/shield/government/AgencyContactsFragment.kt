package com.safenet.shield.government

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class AgencyContactsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply {
            setPadding(32, 32, 32, 32)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val title = TextView(context).apply {
            text = "Government Agency Contacts"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val agencies = listOf(
            Triple("Kenya Police Service", "Emergency: 999\nGeneral: +254-20-341411", "Cybercrime Unit"),
            Triple("Communications Authority", "+254-20-4242000", "ICT & Telecommunications"),
            Triple("DCI - Cybercrime Unit", "+254-20-2240000", "Digital Forensics & Investigation"),
            Triple("Kenya Bureau of Standards", "+254-20-6948000", "Standards & Compliance")
        )
        
        agencies.forEach { (name, contact, dept) ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }
            
            val agencyName = TextView(context).apply {
                text = name
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(agencyName)
            
            val department = TextView(context).apply {
                text = dept
                textSize = 14f
                setPadding(0, 4, 0, 8)
                alpha = 0.8f
            }
            cardLayout.addView(department)
            
            val contactInfo = TextView(context).apply {
                text = contact
                textSize = 14f
            }
            cardLayout.addView(contactInfo)
            
            card.addView(cardLayout)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
