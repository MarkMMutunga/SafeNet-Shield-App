package com.safenet.shield.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class ThreatAnalysisFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Threat Analysis"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val threats = listOf(
            Triple("Phishing Attacks", "High", "#FF5722"),
            Triple("Malware Infections", "Medium", "#FF9800"), 
            Triple("Data Breaches", "Medium", "#FF9800"),
            Triple("Identity Theft", "Low", "#4CAF50")
        )
        
        threats.forEach { (name, level, color) ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val cardLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            
            val threatName = TextView(context).apply {
                text = name
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(threatName)
            
            val threatLevel = TextView(context).apply {
                text = "Risk Level: $level"
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor(color))
                setPadding(0, 8, 0, 0)
            }
            cardLayout.addView(threatLevel)
            
            card.addView(cardLayout)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
