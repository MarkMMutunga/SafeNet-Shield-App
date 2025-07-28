package com.safenet.shield.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class SafetyRecommendationsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Safety Recommendations"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val recommendations = listOf(
            "🔐 Enable two-factor authentication on all accounts",
            "🆙 Update your operating system and apps regularly",
            "📧 Be cautious with email attachments and links",
            "🔒 Use strong, unique passwords for each account",
            "🛡️ Install reputable antivirus software",
            "📱 Avoid public WiFi for sensitive activities"
        )
        
        recommendations.forEach { recommendation ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val recommendationText = TextView(context).apply {
                text = recommendation
                textSize = 16f
            }
            
            card.addView(recommendationText)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
