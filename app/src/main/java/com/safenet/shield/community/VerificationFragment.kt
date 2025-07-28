package com.safenet.shield.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class VerificationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Verification Center"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val verificationItems = listOf(
            Triple("✅ Verified Sources", "Check if information comes from trusted sources", "#4CAF50"),
            Triple("🔍 Fact Checking", "Verify claims with multiple sources", "#2196F3"),
            Triple("⚠️ Suspicious Content", "Report potentially false information", "#FF9800"),
            Triple("🛡️ Source Rating", "Community-rated reliability scores", "#9C27B0")
        )
        
        verificationItems.forEach { (title, desc, color) ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val cardLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            
            val titleText = TextView(context).apply {
                text = title
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor(color))
            }
            cardLayout.addView(titleText)
            
            val descText = TextView(context).apply {
                text = desc
                textSize = 14f
                setPadding(0, 8, 0, 0)
            }
            cardLayout.addView(descText)
            
            card.addView(cardLayout)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
