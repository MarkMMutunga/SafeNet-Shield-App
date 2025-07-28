package com.safenet.shield.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class CommunityIntelFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Community Intelligence"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val intel = listOf(
            Triple("Phishing Alert - Westlands", "2 hours ago", "Multiple reports of fake bank SMS"),
            Triple("Scam Warning - CBD", "4 hours ago", "Fake job recruitment ongoing"),
            Triple("Security Breach - Kiambu", "1 day ago", "Data leak at local business")
        )
        
        intel.forEach { (title, time, desc) ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val cardLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            
            val titleText = TextView(context).apply {
                text = title
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(titleText)
            
            val timeText = TextView(context).apply {
                text = time
                textSize = 12f
                alpha = 0.7f
                setPadding(0, 4, 0, 4)
            }
            cardLayout.addView(timeText)
            
            val descText = TextView(context).apply {
                text = desc
                textSize = 14f
            }
            cardLayout.addView(descText)
            
            card.addView(cardLayout)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
