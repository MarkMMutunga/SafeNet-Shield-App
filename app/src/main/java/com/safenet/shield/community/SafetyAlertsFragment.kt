package com.safenet.shield.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class SafetyAlertsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Safety Alerts"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val alerts = listOf(
            Triple("🚨 URGENT", "New ransomware targeting local businesses", "#FF5722"),
            Triple("⚠️ WARNING", "Fake police checkpoint scam reported", "#FF9800"),
            Triple("ℹ️ INFO", "Security awareness week starting Monday", "#2196F3")
        )
        
        alerts.forEach { (level, message, color) ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val cardLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            
            val levelText = TextView(context).apply {
                text = level
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor(color))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(levelText)
            
            val messageText = TextView(context).apply {
                text = message
                textSize = 16f
                setPadding(0, 8, 0, 0)
            }
            cardLayout.addView(messageText)
            
            card.addView(cardLayout)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
